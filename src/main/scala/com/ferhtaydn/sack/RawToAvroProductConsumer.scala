package com.ferhtaydn.sack

import akka.actor.{ Actor, ActorLogging, ActorRef, ActorSystem, Props }
import cakesolutions.kafka.akka.KafkaConsumerActor.{ Confirm, Subscribe }
import cakesolutions.kafka.{ KafkaConsumer, KafkaProducer }
import cakesolutions.kafka.akka.{ ConsumerRecords, KafkaConsumerActor, KafkaProducerActor, Offsets, ProducerRecords }
import com.ferhtaydn.sack.model.Product
import com.typesafe.config.{ Config, ConfigFactory }
import org.apache.kafka.common.serialization.{ ByteArraySerializer, StringDeserializer, StringSerializer }

import scala.util.Random
import scala.concurrent.duration._

object RawToAvroProductConsumerBoot extends App {

  val config = ConfigFactory.load()
  val consumerConfig = config.getConfig("consumer")
  val producerConfig = config.getConfig("producerAvro")

  RawToAvroProductConsumer(consumerConfig, producerConfig)

}

object RawToAvroProductConsumer {

  def apply(consumerConfig: Config, producerConfig: Config): ActorRef = {

    val consumerConf = KafkaConsumer.Conf(consumerConfig, new StringDeserializer, new StringDeserializer)

    val actorConf = KafkaConsumerActor.Conf(1.seconds, 3.seconds)

    val producerConf = KafkaProducer.Conf(producerConfig, new StringSerializer, new ByteArraySerializer)

    val system = ActorSystem("raw-to-avro-product-consumer-system")

    system.actorOf(
      Props(new RawToAvroProductConsumer(consumerConf, actorConf, producerConf)),
      "raw-to-avro-product-consumer-actor"
    )

  }

}

class RawToAvroProductConsumer(
    kafkaConsumerConf: KafkaConsumer.Conf[String, String],
    consumerActorConf: KafkaConsumerActor.Conf,
    kafkaProducerConf: KafkaProducer.Conf[String, Array[Byte]]
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

  def prepareRecord(key: String, value: String): (String, Array[Byte]) = {
    val p = Product("brand" + Random.nextInt(10).toString, 1, 2, 3, 4, "http" + Random.nextInt(10).toString)
    (p.imageUrl, ProductSchema.productAsBytes(p))
  }

  private def processRecords(records: ConsumerRecords[String, String]) = {

    val transformedRecords = records.pairs.map {
      case (key, value) ⇒
        log.info(s"Received [$key, $value]")
        prepareRecord("", value)
    }

    log.info(s"Batch complete, offsets: ${records.offsets}")

    producer ! ProducerRecords.fromKeyValues[String, Array[Byte]](
      "product-csv-avro",
      transformedRecords,
      Some(records.offsets),
      None
    )
  }
}
