package com.ferhtaydn.sack.binary

import akka.actor.{ Actor, ActorLogging, ActorRef, ActorSystem, Props }
import cakesolutions.kafka.KafkaConsumer
import cakesolutions.kafka.akka.KafkaConsumerActor.{ Confirm, Subscribe }
import cakesolutions.kafka.akka.{ ConsumerRecords, KafkaConsumerActor }
import com.ferhtaydn.sack.ProductSchema
import com.typesafe.config.{ Config, ConfigFactory }
import org.apache.kafka.common.serialization.{ ByteArrayDeserializer, StringDeserializer }

import scala.concurrent.duration._

object BinaryProductConsumerBoot extends App {

  val config = ConfigFactory.load()
  val consumerConfig = config.getConfig("kafka.consumer-binary")

  BinaryProductConsumer(consumerConfig)

}

object BinaryProductConsumer {

  def apply(consumerConfig: Config): ActorRef = {

    val consumerConf = KafkaConsumer.Conf(consumerConfig, new StringDeserializer, new ByteArrayDeserializer)

    val actorConf = KafkaConsumerActor.Conf(1.seconds, 3.seconds)

    val system = ActorSystem("binary-product-consumer-system")

    system.actorOf(
      Props(new BinaryProductConsumer(consumerConf, actorConf)),
      "binary-product-consumer-actor"
    )

  }

}

class BinaryProductConsumer(
    kafkaConsumerConf: KafkaConsumer.Conf[String, Array[Byte]],
    consumerActorConf: KafkaConsumerActor.Conf
) extends Actor with ActorLogging {

  val recordsExt = ConsumerRecords.extractor[String, Array[Byte]]
  val inputTopic = "product-csv-binary"

  val consumerActor = context.actorOf(
    KafkaConsumerActor.props(kafkaConsumerConf, consumerActorConf, self),
    "kafka-consumer-actor"
  )

  context.watch(consumerActor)

  consumerActor ! Subscribe.AutoPartition(List(inputTopic))

  override def receive: Receive = {

    case recordsExt(records) ⇒
      log.info("Records from KafkaConsumer:\n")
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
