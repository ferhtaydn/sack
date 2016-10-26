package com.ferhtaydn.sack

import akka.actor.{ Actor, ActorLogging, ActorRef, ActorSystem, Props }
import cakesolutions.kafka.akka.KafkaConsumerActor.{ Confirm, Subscribe }
import cakesolutions.kafka.KafkaConsumer
import cakesolutions.kafka.akka.{ ConsumerRecords, KafkaConsumerActor }
import com.typesafe.config.{ Config, ConfigFactory }
import org.apache.kafka.common.serialization.{ ByteArrayDeserializer, StringDeserializer }

import scala.concurrent.duration._

object AvroProductConsumerBoot extends App {

  val config = ConfigFactory.load()
  val consumerConfig = config.getConfig("consumerAvro")

  AvroProductConsumer(consumerConfig)

}

object AvroProductConsumer {

  def apply(consumerConfig: Config): ActorRef = {

    val consumerConf = KafkaConsumer.Conf(consumerConfig, new StringDeserializer, new ByteArrayDeserializer)

    val actorConf = KafkaConsumerActor.Conf(1.seconds, 3.seconds)

    val system = ActorSystem("avro-product-consumer-system")

    system.actorOf(
      Props(new AvroProductConsumer(consumerConf, actorConf)),
      "avro-product-consumer-actor"
    )

  }

}

class AvroProductConsumer(
    kafkaConsumerConf: KafkaConsumer.Conf[String, Array[Byte]],
    consumerActorConf: KafkaConsumerActor.Conf
) extends Actor with ActorLogging {

  val recordsExt = ConsumerRecords.extractor[String, Array[Byte]]

  val consumerActor = context.actorOf(
    KafkaConsumerActor.props(kafkaConsumerConf, consumerActorConf, self),
    "kafka-consumer-actor"
  )

  context.watch(consumerActor)

  consumerActor ! Subscribe.AutoPartition(List("product-csv-avro"))

  override def receive: Receive = {

    // Records from Kafka
    case recordsExt(records) ⇒
      processRecords(records)

  }

  private def processRecords(records: ConsumerRecords[String, Array[Byte]]) = {

    records.pairs.foreach {
      case (key, value) ⇒
        log.info(s"Received [$key, ${ProductSchema.productFromBytes(value)}]")
    }

    log.info(s"Batch complete, offsets: ${records.offsets}")
    consumerActor ! Confirm(records.offsets, commit = false)

  }
}
