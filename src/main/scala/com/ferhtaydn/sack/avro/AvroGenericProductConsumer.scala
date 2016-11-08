package com.ferhtaydn.sack.avro

import akka.actor.{ Actor, ActorLogging, ActorRef, ActorSystem, Props }
import cakesolutions.kafka.KafkaConsumer
import cakesolutions.kafka.akka.{ ConsumerRecords, KafkaConsumerActor }
import cakesolutions.kafka.akka.KafkaConsumerActor.{ Confirm, Subscribe }
import com.ferhtaydn.sack.ProductSchema
import com.typesafe.config.{ Config, ConfigFactory }
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.common.serialization.StringDeserializer

import scala.concurrent.duration._

object AvroGenericProductConsumerBoot extends App {

  val config = ConfigFactory.load()
  val consumerConfig = config.getConfig("kafka.consumer-avro")

  AvroGenericProductConsumer(consumerConfig)

}

object AvroGenericProductConsumer {

  def apply(consumerConfig: Config): ActorRef = {

    val consumerConf = KafkaConsumer.Conf(
      consumerConfig,
      new StringDeserializer,
      GenericAvroDeserializer(
        new CachedSchemaRegistryClient(consumerConfig.getString("schema.registry.url"), 100)
      )
    )

    val actorConf = KafkaConsumerActor.Conf(1.seconds, 3.seconds)

    val system = ActorSystem("avro-product-consumer-system")

    system.actorOf(
      Props(new AvroGenericProductConsumer(consumerConf, actorConf)),
      "avro-product-consumer-actor"
    )

  }

}

class AvroGenericProductConsumer(
    kafkaConsumerConf: KafkaConsumer.Conf[String, GenericRecord],
    consumerActorConf: KafkaConsumerActor.Conf
) extends Actor with ActorLogging {

  val recordsExt = ConsumerRecords.extractor[String, GenericRecord]
  val inputTopic = "product-csv-avro"

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

  private def processRecords(records: ConsumerRecords[String, GenericRecord]) = {

    records.pairs.foreach {
      case (key, value) ⇒
        log.info(s"Received [$key, $value]")
        log.info(s"Received [$key, ${ProductSchema.productFromRecord(value)}]")
    }

    log.info(s"Batch complete, offsets: ${records.offsets}")
    consumerActor ! Confirm(records.offsets, commit = false)

  }
}