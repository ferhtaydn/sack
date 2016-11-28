/*
 * Copyright 2016 Ferhat Aydın
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ferhtaydn.avro

import akka.actor.{ Actor, ActorLogging, ActorRef, ActorSystem, Props }
import cakesolutions.kafka.akka.KafkaConsumerActor.{ Confirm, Subscribe, Unsubscribe }
import cakesolutions.kafka.akka._
import cakesolutions.kafka.{ KafkaConsumer, KafkaProducer }
import com.ferhtaydn.common.models.{ ProductExt, ProductSchema }
import com.ferhtaydn.common.settings.Settings
import com.ferhtaydn.common.Boot
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.common.serialization.{ StringDeserializer, StringSerializer }

import scala.concurrent.duration._

object RawToAvroGenericProcessorBoot extends Boot {

  val system = ActorSystem("raw-to-avro-generic-processor-system")
  val settings = Settings(system)
  import settings.Kafka.Producer._
  val producerConfig = settings.Kafka.Producer.producerConfig
  val consumerConfig = settings.Kafka.Consumer.consumerConfig

  val consumerConf = KafkaConsumer.Conf(
    new StringDeserializer,
    new StringDeserializer,
    groupId = "csv-raw-consumer"
  ).withConf(consumerConfig)

  val actorConf = KafkaConsumerActor.Conf(1.seconds, 3.seconds)

  val producerConf = KafkaProducer.Conf(
    producerConfig,
    new StringSerializer,
    GenericAvroSerializer(new CachedSchemaRegistryClient(schemaRegistryUrl, 100))
  )

  system.actorOf(
    RawToAvroGenericProcessor.props(consumerConf, actorConf, producerConf),
    "raw-to-avro-generic-processor-actor"
  )

  terminate(system)

}

object RawToAvroGenericProcessor {

  def props(
    consumerConf: KafkaConsumer.Conf[String, String],
    actorConf: KafkaConsumerActor.Conf,
    producerConf: KafkaProducer.Conf[String, GenericRecord]
  ): Props = {
    Props(new RawToAvroGenericProcessor(consumerConf, actorConf, producerConf))
  }

}

class RawToAvroGenericProcessor(
    kafkaConsumerConf: KafkaConsumer.Conf[String, String],
    consumerActorConf: KafkaConsumerActor.Conf,
    kafkaProducerConf: KafkaProducer.Conf[String, GenericRecord]
) extends Actor with ActorLogging {

  val recordsExt = ConsumerRecords.extractor[String, String]
  val inputTopic = "product-csv-raw"
  val outputTopic = "product-csv-avro"

  var consumerActor: ActorRef = _
  var producerActor: ActorRef = _

  override def preStart(): Unit = {

    super.preStart()

    consumerActor = context.actorOf(
      KafkaConsumerActor.props(kafkaConsumerConf, consumerActorConf, self),
      "kafka-consumer-actor"
    )

    context.watch(consumerActor)

    consumerActor ! Subscribe.AutoPartition(List(inputTopic))

    producerActor = context.actorOf(
      KafkaProducerActor.props(kafkaProducerConf),
      "kafka-producer-actor"
    )

    context.watch(producerActor)

  }

  override def postStop(): Unit = {

    consumerActor ! Unsubscribe

    context.children foreach { child ⇒
      context.unwatch(child)
      context.stop(child)
    }

    super.postStop()
  }

  override def receive: Receive = {

    case recordsExt(records) ⇒
      log.info("Records from KafkaConsumer:\n")
      processRecords(records)

    case o: Offsets ⇒
      log.info(s"Response from KafkaProducer, offsets: $o")
      consumerActor ! Confirm(o, commit = false)
  }

  private def processRecords(records: ConsumerRecords[String, String]) = {

    def prepareRecord(key: Option[String], value: String): (String, GenericRecord) = {
      val p = ProductExt.dummyProduct
      (p.barcode, ProductSchema.productToRecord(p))
    }

    val transformedRecords = records.pairs.map {
      case (key, value) ⇒
        log.info(s"Received [$key, $value]")
        prepareRecord(key, value)
    }

    log.info(s"Batch complete, offsets: ${records.offsets}")

    producerActor ! ProducerRecords.fromKeyValues[String, GenericRecord](
      outputTopic,
      transformedRecords,
      Some(records.offsets),
      None
    )
  }
}
