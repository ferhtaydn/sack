package com.ferhtaydn.sack.cassandra

import akka.actor.{ Actor, ActorLogging, ActorRef, ActorSystem, Props }
import cakesolutions.kafka.KafkaConsumer
import cakesolutions.kafka.akka.{ ConsumerRecords, KafkaConsumerActor }
import cakesolutions.kafka.akka.KafkaConsumerActor.{ Confirm, Subscribe, Unsubscribe }
import com.ferhtaydn.sack.settings.Settings
import com.ferhtaydn.sack.{ Boot, ProductSchema }
import io.confluent.kafka.serializers.KafkaAvroDeserializer
import org.apache.avro.generic.GenericRecord
import com.ferhtaydn.sack.settings.TypesafeConfigExtensions._

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
        log.info(s"Received [$key, ${ProductSchema.tproductFromRecord(value.asInstanceOf[GenericRecord])}]")
    }

    log.info(s"Batch complete, offsets: ${records.offsets}")
    consumerActor ! Confirm(records.offsets, commit = false)

  }
}