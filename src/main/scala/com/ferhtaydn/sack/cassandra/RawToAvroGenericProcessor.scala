package com.ferhtaydn.sack.cassandra

import akka.actor.{ Actor, ActorLogging, ActorRef, ActorSystem, Props }
import cakesolutions.kafka.akka.KafkaConsumerActor.{ Confirm, Subscribe, Unsubscribe }
import cakesolutions.kafka.akka._
import cakesolutions.kafka.{ KafkaConsumer, KafkaProducer }
import com.ferhtaydn.sack.http.Models.OK
import com.ferhtaydn.sack.model.TProduct
import com.ferhtaydn.sack.{ Boot, ProductSchema }
import com.ferhtaydn.sack.settings.Settings
import io.confluent.kafka.serializers.{ KafkaAvroDeserializer, KafkaAvroSerializer }
import com.ferhtaydn.sack.settings.TypesafeConfigExtensions._

import scala.concurrent.duration._
import scala.collection.JavaConversions._

/**
 * Cassandra-sink Serialization compatible
 */
object RawToAvroGenericProcessorBoot extends App with Boot {

  val system = ActorSystem("raw-to-avro-generic-processor-system")

  val settings = Settings(system)
  val producerConfig = settings.Kafka.Producer.producerConfig
  val consumerConfig = settings.Kafka.Consumer.consumerConfig

  val kafkaAvroDeserializerForKey = new KafkaAvroDeserializer()
  val kafkaAvroDeserializerForValue = new KafkaAvroDeserializer()
  kafkaAvroDeserializerForKey.configure(consumerConfig.toPropertyMap, true)
  kafkaAvroDeserializerForValue.configure(consumerConfig.toPropertyMap, false)

  val consumerConf = KafkaConsumer.Conf(
    kafkaAvroDeserializerForKey,
    kafkaAvroDeserializerForValue,
    groupId = "csv-raw-consumer"
  ).withConf(consumerConfig)

  val actorConf = KafkaConsumerActor.Conf(1.seconds, 3.seconds)

  val kafkaAvroSerializerForKey = new KafkaAvroSerializer()
  val kafkaAvroSerializerForValue = new KafkaAvroSerializer()
  kafkaAvroSerializerForKey.configure(producerConfig.toPropertyMap, true)
  kafkaAvroSerializerForValue.configure(producerConfig.toPropertyMap, false)

  val producerConf = KafkaProducer.Conf(
    producerConfig,
    kafkaAvroSerializerForKey,
    kafkaAvroSerializerForValue
  )

  system.actorOf(
    RawToAvroGenericProcessor.props(consumerConf, actorConf, producerConf),
    "raw-to-avro-generic-processor-actor"
  )

  terminate(system)

}

object RawToAvroGenericProcessor {

  def props(
    consumerConf: KafkaConsumer.Conf[Object, Object],
    actorConf: KafkaConsumerActor.Conf,
    producerConf: KafkaProducer.Conf[Object, Object]
  ): Props = {
    Props(new RawToAvroGenericProcessor(consumerConf, actorConf, producerConf))
  }
}

class RawToAvroGenericProcessor(
    kafkaConsumerConf: KafkaConsumer.Conf[Object, Object],
    consumerActorConf: KafkaConsumerActor.Conf,
    kafkaProducerConf: KafkaProducer.Conf[Object, Object]
) extends Actor with ActorLogging {

  val recordsExt = ConsumerRecords.extractor[Object, Object]
  val inputTopic = "product-csv-raw"
  val outputTopic = "product-csv-avro"
  val invalidOutputTopic = "product-csv-avro-invalid"

  var consumerActor: ActorRef = _
  var producerActor: ActorRef = _

  override def preStart(): Unit = {

    super.preStart()

    consumerActor = context.actorOf(
      KafkaConsumerActor.props(kafkaConsumerConf, consumerActorConf, self),
      "kafka-consumer-actor"
    )

    context.watch(consumerActor)

    consumerActor ! Subscribe.AutoPartition(List(inputTopic))

    producerActor = context.actorOf(
      KafkaProducerActor.props(kafkaProducerConf),
      "kafka-producer-actor"
    )

    context.watch(producerActor)

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

    case o: Offsets ⇒
      log.info(s"Response from KafkaProducer, offsets: $o")
      consumerActor ! Confirm(o, commit = false)

    case OK ⇒
      log.info(s"Response for invalid values")
  }

  private def processRecords(records: ConsumerRecords[Object, Object]) = {

    val (invalidValues, validRecords) = records.pairs.foldLeft((Seq.empty[String], Seq.empty[TProduct])) {
      case ((v, p), (key, value)) ⇒
        log.info(s"Received [$key, $value]")
        ProductSchema.createTProduct(value.asInstanceOf[String]) match {
          case None     ⇒ (value.asInstanceOf[String] +: v, p)
          case Some(tp) ⇒ (v, tp +: p)
        }
    }

    val transformedRecords = validRecords.map(r ⇒ (r.barcode, ProductSchema.tProductToRecord(r)))

    log.info(s"transformedRecords: $transformedRecords")
    log.info(s"invalid values: $invalidValues")

    log.info(s"Batch complete, offsets: ${records.offsets}")

    producerActor ! ProducerRecords.fromKeyValues[Object, Object](
      outputTopic,
      transformedRecords,
      Some(records.offsets),
      None
    )

    producerActor ! ProducerRecords.fromKeyValues[Object, Object](
      invalidOutputTopic,
      invalidValues.map(s ⇒ (s, s)),
      Some(OK),
      None
    )
  }
}
