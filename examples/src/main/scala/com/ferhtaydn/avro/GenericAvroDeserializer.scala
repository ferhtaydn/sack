/*
 * Copyright 2016 Ferhat Aydın
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ferhtaydn.avro

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
