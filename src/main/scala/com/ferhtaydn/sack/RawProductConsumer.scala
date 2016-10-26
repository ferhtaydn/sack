package com.ferhtaydn.sack

import akka.actor.{ Actor, ActorLogging, ActorRef, ActorSystem, Props }
import cakesolutions.kafka.akka.KafkaConsumerActor.{ Confirm, Subscribe }
import cakesolutions.kafka.{ KafkaConsumer, KafkaProducer }
import cakesolutions.kafka.akka._
import com.typesafe.config.{ Config, ConfigFactory }
import org.apache.kafka.common.serialization.{ StringDeserializer, StringSerializer }

import scala.concurrent.duration._

object RawProductConsumerBoot extends App {

  val config = ConfigFactory.load()
  val consumerConfig = config.getConfig("consumer")
  val producerConfig = config.getConfig("producer")

  RawProductConsumer(consumerConfig, producerConfig)

}

object RawProductConsumer {

  def apply(consumerConfig: Config, producerConfig: Config): ActorRef = {

    val consumerConf = KafkaConsumer.Conf(consumerConfig, new StringDeserializer, new StringDeserializer)

    val actorConf = KafkaConsumerActor.Conf(1.seconds, 3.seconds)

    val producerConf = KafkaProducer.Conf(producerConfig, new StringSerializer, new StringSerializer)

    val system = ActorSystem("raw-product-consumer-system")

    system.actorOf(
      Props(new RawProductConsumer(consumerConf, actorConf, producerConf)),
      "raw-product-consumer-actor"
    )

  }

}

class RawProductConsumer(
    kafkaConsumerConf: KafkaConsumer.Conf[String, String],
    consumerActorConf: KafkaConsumerActor.Conf,
    kafkaProducerConf: KafkaProducer.Conf[String, String]
) extends Actor with ActorLogging {

  val recordsExt = ConsumerRecords.extractor[String, String]

  val consumerActor = context.actorOf(
    KafkaConsumerActor.props(kafkaConsumerConf, consumerActorConf, self),
    "kafka-consumer-actor"
  )

  context.watch(consumerActor)

  val producer = context.actorOf(
    KafkaProducerActor.props(kafkaProducerConf),
    "kafka-producer-actor"
  )

  consumerActor ! Subscribe.AutoPartition(List("product-csv-raw"))

  override def receive: Receive = {

    // Records from Kafka
    case recordsExt(records) ⇒
      processRecords(records)

    // Confirmed Offsets from KafkaProducer
    case o: Offsets ⇒
      log.info(s"response from producer, offsets: $o")
      consumerActor ! Confirm(o, commit = false)
  }

  private def processRecords(records: ConsumerRecords[String, String]) = {

    val transformedRecords = records.pairs.map {
      case (key, value) ⇒
        log.info(s"Received [$key, $value]")
        (value, value.toUpperCase())
    }

    log.info(s"Batch complete, offsets: ${records.offsets}")

    producer ! ProducerRecords.fromKeyValues[String, String](
      "product-csv-raw-uppercase",
      transformedRecords,
      Some(records.offsets),
      None
    )
  }
}