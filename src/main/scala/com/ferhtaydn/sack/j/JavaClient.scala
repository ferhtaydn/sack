package com.ferhtaydn.sack.j

import java.util.Properties

import com.ferhtaydn.sack.ProductSchema
import org.apache.kafka.clients.consumer.{ ConsumerConfig, KafkaConsumer }
import org.apache.kafka.clients.producer.{ KafkaProducer, ProducerConfig, ProducerRecord }
import org.apache.kafka.common.serialization.StringDeserializer
import io.confluent.kafka.serializers.KafkaAvroSerializer
import collection.JavaConversions._

/**
 * Created by ferhat.aydin on 11/2/16.
 */
object JavaClient extends App {

  val producerProps = new Properties()
  producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
  producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, classOf[KafkaAvroSerializer].getName)
  producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, classOf[KafkaAvroSerializer].getName)
  producerProps.put("schema.registry.url", "http://localhost:8081")

  val kafkaProducer = new KafkaProducer[Object, Object](producerProps)

  val consumerProperties = new Properties()
  consumerProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
  consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, classOf[StringDeserializer].getName)
  consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, classOf[StringDeserializer].getName)
  consumerProperties.put("schema.registry.url", "http://localhost:8081")
  consumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, "product-csv-raw")
  consumerProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

  val kafkaConsumer = new KafkaConsumer[String, String](consumerProperties, new StringDeserializer, new StringDeserializer)

  kafkaConsumer.subscribe(Set("product-csv-raw"))

  while (true) {

    val consumerRecords = kafkaConsumer.poll(Long.MaxValue)

    consumerRecords.foreach { r ⇒
      val record = new ProducerRecord[Object, Object]("product-csv-avro", "asd",
        ProductSchema.productToRecord(ProductSchema.dummyProduct))
      kafkaProducer.send(record)
    }
    kafkaProducer.flush()

  }

}
