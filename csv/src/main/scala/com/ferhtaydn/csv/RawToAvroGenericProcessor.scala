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

package com.ferhtaydn.csv

import akka.actor.{ Actor, ActorLogging, ActorRef, ActorSystem, Props }
import cakesolutions.kafka.akka.KafkaConsumerActor.{ Confirm, Subscribe, Unsubscribe }
import cakesolutions.kafka.akka._
import cakesolutions.kafka.{ KafkaConsumer, KafkaProducer }
import com.ferhtaydn.common.Boot
import com.ferhtaydn.common.models.Product
import com.ferhtaydn.common.models.{ ProductExt, ProductSchema }
import com.ferhtaydn.common.settings.Settings
import io.confluent.kafka.serializers.{ KafkaAvroDeserializer, KafkaAvroSerializer }
import com.ferhtaydn.common.settings.TypesafeConfigExtensions._

import scala.concurrent.duration._
import scala.collection.JavaConversions._

/**
 * Cassandra-sink Serialization compatible
 */
object RawToAvroGenericProcessorBoot extends Boot {

  val system = ActorSystem("raw-to-avro-generic-processor-system")

  val settings = Settings(system)
  val producerConfig = settings.Kafka.Producer.producerConfig
  val consumerConfig = settings.Kafka.Consumer.consumerConfig

  val kafkaAvroDeserializerForKey = new KafkaAvroDeserializer()
  val kafkaAvroDeserializerForValue = new KafkaAvroDeserializer()
  kafkaAvroDeserializerForKey.configure(consumerConfig.toPropertyMap, true)
  kafkaAvroDeserializerForValue.configure(consumerConfig.toPropertyMap, false)

  val consumerConf = KafkaConsumer.Conf(
    kafkaAvroDeserializerForKey,
    kafkaAvroDeserializerForValue,
    groupId = "csv-raw-consumer"
  ).withConf(consumerConfig)

  val actorConf = KafkaConsumerActor.Conf(1.seconds, 3.seconds)

  val kafkaAvroSerializerForKey = new KafkaAvroSerializer()
  val kafkaAvroSerializerForValue = new KafkaAvroSerializer()
  kafkaAvroSerializerForKey.configure(producerConfig.toPropertyMap, true)
  kafkaAvroSerializerForValue.configure(producerConfig.toPropertyMap, false)

  val producerConf = KafkaProducer.Conf(
    producerConfig,
    kafkaAvroSerializerForKey,
    kafkaAvroSerializerForValue
  )

  system.actorOf(
    RawToAvroGenericProcessor.props(consumerConf, actorConf, producerConf),
    "raw-to-avro-generic-processor-actor"
  )

  terminate(system)

}

object RawToAvroGenericProcessor {

  case object OK

  def props(
    consumerConf: KafkaConsumer.Conf[Object, Object],
    actorConf: KafkaConsumerActor.Conf,
    producerConf: KafkaProducer.Conf[Object, Object]
  ): Props = {
    Props(new RawToAvroGenericProcessor(consumerConf, actorConf, producerConf))
  }
}

class RawToAvroGenericProcessor(
    kafkaConsumerConf: KafkaConsumer.Conf[Object, Object],
    consumerActorConf: KafkaConsumerActor.Conf,
    kafkaProducerConf: KafkaProducer.Conf[Object, Object]
) extends Actor with ActorLogging {

  import RawToAvroGenericProcessor.OK

  val recordsExt = ConsumerRecords.extractor[Object, Object]
  val inputTopic = "product-csv-raw"
  val outputTopic = "product-csv-avro"
  val invalidOutputTopic = "product-csv-avro-invalid"

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

    case OK ⇒
      log.info(s"Response for invalid values")
  }

  private def processRecords(records: ConsumerRecords[Object, Object]) = {

    val (invalidValues, validRecords) = records.pairs.foldLeft((Seq.empty[String], Seq.empty[Product])) {
      case ((v, p), (key, value)) ⇒
        //log.info(s"Received [$key, $value]")
        ProductExt.createProduct(value.asInstanceOf[String]).filter(ProductExt.isValid) match {
          case None ⇒ (value.asInstanceOf[String] +: v, p)
          case Some(tp) ⇒ (v, tp +: p)
        }
    }

    val transformedRecords = validRecords.map(r ⇒ (r.barcode, ProductSchema.productToRecord(r)))

    log.info(s"transformedRecords: ${transformedRecords.length}")
    log.info(s"invalid values: ${invalidValues.length}")

    log.info(s"Batch complete, offsets: ${records.offsets}")

    producerActor ! ProducerRecords.fromKeyValues[Object, Object](
      outputTopic,
      transformedRecords,
      Some(records.offsets),
      None
    )

    producerActor ! ProducerRecords.fromKeyValues[Object, Object](
      invalidOutputTopic,
      invalidValues.map(s ⇒ (s, s)),
      Some(OK),
      None
    )
  }
}
