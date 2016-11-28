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
import cakesolutions.kafka.KafkaConsumer
import cakesolutions.kafka.akka.{ ConsumerRecords, KafkaConsumerActor }
import cakesolutions.kafka.akka.KafkaConsumerActor.{ Confirm, Subscribe, Unsubscribe }
import com.ferhtaydn.common.Boot
import com.ferhtaydn.common.settings.Settings
import com.ferhtaydn.common.models.ProductSchema
import io.confluent.kafka.serializers.KafkaAvroDeserializer
import org.apache.avro.generic.GenericRecord
import com.ferhtaydn.common.settings.TypesafeConfigExtensions._

import scala.collection.JavaConversions._
import scala.concurrent.duration._

object AvroGenericProductConsumerBoot extends App with Boot {

  val system = ActorSystem("avro-product-consumer-system")
  val settings = Settings(system)
  val consumerConfig = settings.Kafka.Consumer.consumerConfig

  val kafkaAvroDeserializerForKey = new KafkaAvroDeserializer()
  val kafkaAvroDeserializerForValue = new KafkaAvroDeserializer()
  kafkaAvroDeserializerForKey.configure(consumerConfig.toPropertyMap, true)
  kafkaAvroDeserializerForValue.configure(consumerConfig.toPropertyMap, false)

  val consumerConf = KafkaConsumer.Conf(
    kafkaAvroDeserializerForKey,
    kafkaAvroDeserializerForValue,
    groupId = "csv-avro-consumer"
  ).withConf(consumerConfig)

  val actorConf = KafkaConsumerActor.Conf(1.seconds, 3.seconds)

  system.actorOf(
    AvroGenericProductConsumer.props(consumerConf, actorConf),
    "avro-product-consumer-actor"
  )

  terminate(system)

}

object AvroGenericProductConsumer {

  def props(
    consumerConf: KafkaConsumer.Conf[Object, Object],
    actorConf: KafkaConsumerActor.Conf
  ): Props = {
    Props(new AvroGenericProductConsumer(consumerConf, actorConf))
  }
}

class AvroGenericProductConsumer(
    kafkaConsumerConf: KafkaConsumer.Conf[Object, Object],
    consumerActorConf: KafkaConsumerActor.Conf
) extends Actor with ActorLogging {

  val recordsExt = ConsumerRecords.extractor[Object, Object]
  val inputTopic = "product-csv-avro"

  var consumerActor: ActorRef = _

  override def preStart(): Unit = {

    super.preStart()

    consumerActor = context.actorOf(
      KafkaConsumerActor.props(kafkaConsumerConf, consumerActorConf, self),
      "kafka-consumer-actor"
    )

    context.watch(consumerActor)

    consumerActor ! Subscribe.AutoPartition(List(inputTopic))

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

  }

  private def processRecords(records: ConsumerRecords[Object, Object]) = {

    records.pairs.foreach {
      case (key, value) ⇒
        log.info(s"Received [$key, $value]")
        log.info(s"Received [$key, ${ProductSchema.productFromRecord(value.asInstanceOf[GenericRecord])}]")
    }

    log.info(s"Batch complete, offsets: ${records.offsets}")
    consumerActor ! Confirm(records.offsets, commit = false)

  }
}
