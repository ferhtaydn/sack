package com.ferhtaydn.sack.avrokafka

import java.util

import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient
import io.confluent.kafka.serializers.{ AbstractKafkaAvroSerializer, KafkaAvroSerializerConfig }
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.common.serialization.Serializer

import collection.JavaConversions._

sealed abstract class KafkaGenericAvroSerializer(var isKey: Boolean) extends AbstractKafkaAvroSerializer with Serializer[GenericRecord] {

  override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = {
    this.isKey = isKey
    configure(new KafkaAvroSerializerConfig(configs))
  }

  override def serialize(topic: String, data: GenericRecord): Array[Byte] = {
    serializeImpl(KafkaGenericAvroSerializer.getSubjectName(topic, isKey), data)
  }

  override def close(): Unit = {}

}

object KafkaGenericAvroSerializer {

  def apply(isKey: Boolean): KafkaGenericAvroSerializer = new KafkaGenericAvroSerializer(isKey) {}

  def apply(client: SchemaRegistryClient, isKey: Boolean): KafkaGenericAvroSerializer = new KafkaGenericAvroSerializer(isKey) {
    schemaRegistry = client
  }

  def apply(client: SchemaRegistryClient, props: Map[String, _], isKey: Boolean): KafkaGenericAvroSerializer = new KafkaGenericAvroSerializer(isKey) {
    schemaRegistry = client
    configure(serializerConfig(props))
  }

  def getSubjectName(topic: String, isKey: Boolean): String = if (isKey) topic + "-key" else topic + "-value"

}
