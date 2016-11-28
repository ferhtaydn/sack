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
