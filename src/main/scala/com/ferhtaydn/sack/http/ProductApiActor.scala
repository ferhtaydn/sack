package com.ferhtaydn.sack.http

import akka.actor.{ Actor, ActorLogging, Props }
import cakesolutions.kafka.KafkaProducer
import cakesolutions.kafka.akka.{ KafkaProducerActor, ProducerRecords }
import com.ferhtaydn.sack.ProductSchema
import com.ferhtaydn.sack.avro.GenericAvroSerializer
import com.typesafe.config.ConfigFactory
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient
import org.apache.kafka.common.serialization.StringSerializer
import com.ferhtaydn.sack.model.Product
import com.ferhtaydn.sack.settings.SettingsActor
import org.apache.avro.generic.GenericRecord

class ProductApiActor extends Actor with SettingsActor with ActorLogging {

  val outputTopic = "product-http-avro"

  val config = ConfigFactory.load()
  val producerConfig = settings.Kafka.Producer.producerConfig

  import settings.Kafka.Producer._

  val producerConf = KafkaProducer.Conf(
    producerConfig,
    new StringSerializer,
    GenericAvroSerializer(new CachedSchemaRegistryClient(schemaRegistryUrl, 100))
  )

  val producer = context.actorOf(
    KafkaProducerActor.props(producerConf),
    "kafka-producer-actor"
  )

  case object OK

  case class Products(products: Seq[Product])

  def process(products: Seq[Product]): Unit = {

    val transformedProducts = products.map(p ⇒ (p.imageUrl, ProductSchema.productToRecord(p)))

    producer ! ProducerRecords.fromKeyValues[String, GenericRecord](
      outputTopic,
      transformedProducts,
      Some(OK),
      None
    )
  }

  override def receive: Receive = {

    case Products(products) ⇒
      process(products)

    case OK ⇒
      log.info(s"product is added to the kafka-log: $outputTopic")
  }
}

object ProductApiActor {

  def props(): Props = Props(new ProductApiActor)

}
