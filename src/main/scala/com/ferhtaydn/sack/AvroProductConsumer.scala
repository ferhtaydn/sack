package com.ferhtaydn.sack

import akka.actor.{ Actor, ActorLogging, ActorRef, ActorSystem, Props }
import cakesolutions.kafka.akka.KafkaConsumerActor.{ Confirm, Subscribe }
import cakesolutions.kafka.{ KafkaConsumer, KafkaProducer }
import cakesolutions.kafka.akka._
import com.typesafe.config.{ Config, ConfigFactory }
import io.confluent.kafka.serializers.KafkaAvroSerializer
import org.apache.kafka.common.serialization.{ StringDeserializer, StringSerializer }

import scala.concurrent.duration._

object AvroProductConsumerBoot extends App {

  val config = ConfigFactory.load()
  val consumerConfig = config.getConfig("consumer")
  val producerConfig = config.getConfig("producer-avro")

  AvroProductConsumer(consumerConfig, producerConfig)

}

object AvroProductConsumer {

  def apply(consumerConfig: Config, producerConfig: Config): ActorRef = {

    val consumerConf = KafkaConsumer.Conf(consumerConfig, new StringDeserializer, new StringDeserializer)

    val actorConf = KafkaConsumerActor.Conf(1.seconds, 3.seconds)

    val producerConf = KafkaProducer.Conf(producerConfig, new StringSerializer, new KafkaAvroSerializer)

    val system = ActorSystem("avro-product-consumer-system")

    system.actorOf(
      Props(new AvroProductConsumer(consumerConf, actorConf, producerConf)),
      "avro-product-consumer-actor"
    )

  }

}

class AvroProductConsumer(
    kafkaConsumerConf: KafkaConsumer.Conf[String, String],
    consumerActorConf: KafkaConsumerActor.Conf,
    kafkaProducerConf: KafkaProducer.Conf[String, AnyRef]
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
      consumerActor ! Confirm(o, commit = true)
  }

  private def processRecords(records: ConsumerRecords[String, String]) = {

    val transformedRecords = records.pairs.map {
      case (key, value) ⇒
        log.info(s"Received [$key, $value]")
        (value, com.ferhtaydn.sack.model.Product("brand" + scala.util.Random.nextInt(100).toString, 1, 2, 3, 4, value.toUpperCase()))
    }

    log.info(s"Batch complete, offsets: ${records.offsets}")

    producer ! ProducerRecords.fromKeyValues[String, com.ferhtaydn.sack.model.Product](
      "product-csv-avro1",
      transformedRecords,
      Some(records.offsets),
      None
    )
  }
}
