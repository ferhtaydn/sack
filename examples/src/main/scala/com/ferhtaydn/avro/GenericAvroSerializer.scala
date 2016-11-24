package com.ferhtaydn.avro

import java.util

import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient
import io.confluent.kafka.serializers.KafkaAvroSerializer
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.common.serialization.Serializer

sealed trait GenericAvroSerializer extends Serializer[GenericRecord] {

  def inner: KafkaAvroSerializer

  override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = inner.configure(configs, isKey)

  override def serialize(topic: String, data: GenericRecord): Array[Byte] = inner.serialize(topic, data)

  override def close(): Unit = inner.close()

}

object GenericAvroSerializer {

  def apply(): GenericAvroSerializer = new GenericAvroSerializer {
    override def inner: KafkaAvroSerializer = new KafkaAvroSerializer
  }

  def apply(client: SchemaRegistryClient): GenericAvroSerializer = new GenericAvroSerializer {
    override def inner = new KafkaAvroSerializer(client)
  }

}
