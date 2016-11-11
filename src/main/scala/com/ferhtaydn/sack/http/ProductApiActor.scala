package com.ferhtaydn.sack.http

import akka.actor.{ Actor, ActorLogging, Props }
import cakesolutions.kafka.KafkaProducer
import cakesolutions.kafka.akka.{ KafkaProducerActor, ProducerRecords }
import com.ferhtaydn.sack.ProductSchema
import com.ferhtaydn.sack.model.{ Product, TProduct }
import com.ferhtaydn.sack.settings.SettingsActor
import io.confluent.kafka.serializers.KafkaAvroSerializer
import com.ferhtaydn.sack.settings.TypesafeConfigExtensions._

import scala.collection.JavaConversions._
import com.ferhtaydn.sack.http.Models._

class ProductApiActor extends Actor with SettingsActor with ActorLogging {

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

    val transformedProducts = products.map(p ⇒ (p.imageUrl, ProductSchema.productToRecord(p)))

    producer ! ProducerRecords.fromKeyValues[Object, Object](
      outputTopic,
      transformedProducts,
      Some(OK),
      None
    )
  }

  def tprocess(products: Seq[TProduct]): Unit = {

    val transformedProducts = products.map(p ⇒ (p.barcode, ProductSchema.tProductToRecord(p)))

    producer ! ProducerRecords.fromKeyValues[Object, Object](
      outputTopic,
      transformedProducts,
      Some(OK),
      None
    )
  }

  override def receive: Receive = {

    case Products(products) ⇒
      log.info(s"Received products: $products")
      process(products)

    case TProducts(tproducts) ⇒
      log.info(s"Received tproducts: $tproducts")
      tprocess(tproducts)

    case OK ⇒
      log.info(s"product is added to the kafka-log: $outputTopic")
  }
}

object ProductApiActor {

  def props(): Props = Props(new ProductApiActor)

}
