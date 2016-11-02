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
import org.apache.avro.generic.GenericRecord

import scala.util.Random

class ProductProducerActor extends Actor with ActorLogging {

  val outputTopic = "product-http-avro"

  val config = ConfigFactory.load()
  val producerConfig = config.getConfig("producerAvro")

  val producerConf = KafkaProducer.Conf(
    producerConfig,
    new StringSerializer,
    GenericAvroSerializer(new CachedSchemaRegistryClient(producerConfig.getString("schema.registry.url"), 100))
  )

  val producer = context.actorOf(
    KafkaProducerActor.props(producerConf),
    "kafka-producer-actor"
  )

  case object OK

  def process(p: Product): Unit = {

    val producerRecord = (p.imageUrl, ProductSchema.productToRecord(p))

    producer ! ProducerRecords.fromKeyValues[String, GenericRecord](
      outputTopic,
      Seq(producerRecord),
      Some(OK),
      None
    )
  }

  override def receive: Receive = {
    case product: Product ⇒
      process(product)

    case OK ⇒
      log.info(s"product is added to the kafka-log: $outputTopic")
  }
}

object ProductProducerActor {

  def props(): Props = Props(new ProductProducerActor)

}
