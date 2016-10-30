package com.ferhtaydn.sack.avro

import java.util

import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient
import io.confluent.kafka.serializers.KafkaAvroDeserializer
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.common.serialization.Deserializer

sealed trait GenericAvroDeserializer extends Deserializer[GenericRecord] {

  def inner: KafkaAvroDeserializer

  override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = inner.configure(configs, isKey)

  override def close(): Unit = inner.close()

  override def deserialize(topic: String, data: Array[Byte]): GenericRecord = {
    inner.deserialize(topic, data).asInstanceOf[GenericRecord]
  }

}

object GenericAvroDeserializer {

  def apply(): GenericAvroDeserializer = new GenericAvroDeserializer {
    override def inner: KafkaAvroDeserializer = new KafkaAvroDeserializer
  }

  def apply(client: SchemaRegistryClient): GenericAvroDeserializer = new GenericAvroDeserializer {
    override def inner = new KafkaAvroDeserializer(client)
  }

}
