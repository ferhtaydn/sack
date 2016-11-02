package com.ferhtaydn.sack.binary

import akka.actor.{ Actor, ActorLogging, ActorRef, ActorSystem, Props }
import cakesolutions.kafka.akka.KafkaConsumerActor.{ Confirm, Subscribe }
import cakesolutions.kafka.akka._
import cakesolutions.kafka.{ KafkaConsumer, KafkaProducer }
import com.ferhtaydn.sack.ProductSchema
import com.ferhtaydn.sack.model.Product
import com.typesafe.config.{ Config, ConfigFactory }
import org.apache.kafka.common.serialization.{ ByteArraySerializer, StringDeserializer, StringSerializer }

import scala.concurrent.duration._
import scala.util.Random

object RawToBinaryProcessorBoot extends App {

  val config = ConfigFactory.load()
  val consumerConfig = config.getConfig("consumerRaw")
  val producerConfig = config.getConfig("producerBinary")

  RawToBinaryProcessor(consumerConfig, producerConfig)

}

object RawToBinaryProcessor {

  def apply(consumerConfig: Config, producerConfig: Config): ActorRef = {

    val consumerConf = KafkaConsumer.Conf(consumerConfig, new StringDeserializer, new StringDeserializer)

    val actorConf = KafkaConsumerActor.Conf(1.seconds, 3.seconds)

    val producerConf = KafkaProducer.Conf(producerConfig, new StringSerializer, new ByteArraySerializer)

    val system = ActorSystem("raw-to-binary-processor-system")

    system.actorOf(
      Props(new RawToBinaryProcessor(consumerConf, actorConf, producerConf)),
      "raw-to-binary-processor-actor"
    )

  }

}

class RawToBinaryProcessor(
    kafkaConsumerConf: KafkaConsumer.Conf[String, String],
    consumerActorConf: KafkaConsumerActor.Conf,
    kafkaProducerConf: KafkaProducer.Conf[String, Array[Byte]]
) extends Actor with ActorLogging {

  val recordsExt = ConsumerRecords.extractor[String, String]
  val inputTopic = "product-csv-raw"
  val outputTopic = "product-csv-binary"

  val consumerActor = context.actorOf(
    KafkaConsumerActor.props(kafkaConsumerConf, consumerActorConf, self),
    "kafka-consumer-actor"
  )

  context.watch(consumerActor)

  val producer = context.actorOf(
    KafkaProducerActor.props(kafkaProducerConf),
    "kafka-producer-actor"
  )

  consumerActor ! Subscribe.AutoPartition(List(inputTopic))

  override def receive: Receive = {

    case recordsExt(records) ⇒
      log.info("Records from KafkaConsumer:\n")
      processRecords(records)

    case o: Offsets ⇒
      log.info(s"Response from KafkaProducer, offsets: $o")
      consumerActor ! Confirm(o, commit = false)
  }

  private def processRecords(records: ConsumerRecords[String, String]) = {

    def prepareRecord(key: Option[String], value: String): (String, Array[Byte]) = {
      val p = ProductSchema.dummyProduct
      (p.imageUrl, ProductSchema.productAsBytes(p))
    }

    val transformedRecords = records.pairs.map {
      case (key, value) ⇒
        log.info(s"Received [$key, $value]")
        prepareRecord(key, value)
    }

    log.info(s"Batch complete, offsets: ${records.offsets}")

    producer ! ProducerRecords.fromKeyValues[String, Array[Byte]](
      outputTopic,
      transformedRecords,
      Some(records.offsets),
      None
    )
  }
}
