package com.ferhtaydn.http

import akka.actor.{ Actor, ActorLogging, Props }
import cakesolutions.kafka.KafkaProducer
import cakesolutions.kafka.akka.{ KafkaProducerActor, ProducerRecords }
import com.ferhtaydn.http.ProductApiActor.{ NOK, OK }
import com.ferhtaydn.common.models.Product
import com.ferhtaydn.common.models.{ ProductExt, ProductSchema }
import com.ferhtaydn.common.settings.SettingsActor
import io.confluent.kafka.serializers.KafkaAvroSerializer
import com.ferhtaydn.common.settings.TypesafeConfigExtensions._

import scala.collection.JavaConversions._

class ProductApiActor extends Actor with SettingsActor with ActorLogging {

  val invalidTopic = "product-http-avro-invalid"
  val outputTopic = "product-http-avro"

  val producerConfig = settings.Kafka.Producer.producerConfig

  val kafkaAvroSerializerForKey = new KafkaAvroSerializer()
  val kafkaAvroSerializerForValue = new KafkaAvroSerializer()
  kafkaAvroSerializerForKey.configure(producerConfig.toPropertyMap, true)
  kafkaAvroSerializerForValue.configure(producerConfig.toPropertyMap, false)

  val producerConf = KafkaProducer.Conf(
    producerConfig,
    kafkaAvroSerializerForKey,
    kafkaAvroSerializerForValue
  )

  val producer = context.actorOf(
    KafkaProducerActor.props(producerConf),
    "kafka-producer-actor"
  )

  def process(products: Seq[Product]): Unit = {

    log.info(s"beginning of process method for products")

    val (valid, invalid) = products.partition(ProductExt.isValid)

    val validRecords = valid.map(p ⇒ (p.barcode, ProductSchema.productToRecord(p)))
    val invalidRecords = invalid.map(p ⇒ (p.barcode, ProductSchema.productToRecord(p)))

    log.info(s"validRecords: ${validRecords.length}")

    if (validRecords.nonEmpty) {
      producer ! ProducerRecords.fromKeyValues[Object, Object](
        outputTopic,
        validRecords,
        Some(OK),
        None
      )
    }

    log.info(s"invalidRecords: ${invalidRecords.length}")

    if (invalidRecords.nonEmpty) {
      producer ! ProducerRecords.fromKeyValues[Object, Object](
        invalidTopic,
        invalidRecords,
        Some(NOK),
        None
      )
    }
  }

  override def receive: Receive = {

    case Products(products) ⇒
      log.info(s"Received products: ${products.length}")
      process(products)

    case OK ⇒
      log.info(s"valid products are added to the kafka-log: $outputTopic")

    case NOK ⇒
      log.info(s"invalid products are added to the kafka-log: $invalidTopic")

  }
}

object ProductApiActor {

  case object OK

  case object NOK

  def props(): Props = Props(new ProductApiActor)

}
