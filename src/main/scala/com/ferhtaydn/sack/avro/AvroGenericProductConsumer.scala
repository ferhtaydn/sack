package com.ferhtaydn.sack.avro

import akka.actor.{ Actor, ActorLogging, ActorRef, ActorSystem, Props }
import cakesolutions.kafka.KafkaConsumer
import cakesolutions.kafka.akka.{ ConsumerRecords, KafkaConsumerActor }
import cakesolutions.kafka.akka.KafkaConsumerActor.{ Confirm, Subscribe, Unsubscribe }
import com.ferhtaydn.sack.settings.Settings
import com.ferhtaydn.sack.{ Boot, ProductSchema }
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.common.serialization.StringDeserializer

import scala.concurrent.duration._

object AvroGenericProductConsumerBoot extends App with Boot {

  val system = ActorSystem("avro-product-consumer-system")
  val settings = Settings(system)
  val consumerConfig = settings.Kafka.Consumer.consumerConfig
  import settings.Kafka.Consumer._

  val consumerConf = KafkaConsumer.Conf(
    new StringDeserializer,
    GenericAvroDeserializer(new CachedSchemaRegistryClient(schemaRegistryUrl, 100)),
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
    consumerConf: KafkaConsumer.Conf[String, GenericRecord],
    actorConf: KafkaConsumerActor.Conf
  ): Props = {
    Props(new AvroGenericProductConsumer(consumerConf, actorConf))
  }
}

class AvroGenericProductConsumer(
    kafkaConsumerConf: KafkaConsumer.Conf[String, GenericRecord],
    consumerActorConf: KafkaConsumerActor.Conf
) extends Actor with ActorLogging {

  val recordsExt = ConsumerRecords.extractor[String, GenericRecord]
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