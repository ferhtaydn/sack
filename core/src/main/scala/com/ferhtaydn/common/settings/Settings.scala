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

package com.ferhtaydn.common.settings

import akka.actor.{ ExtendedActorSystem, Extension, ExtensionId }
import com.typesafe.config.Config

class SettingsImpl(config: Config) extends Extension {

  object Kafka {
    private val kafkaConfig = config.getConfig("kafka")

    object Producer {
      val producerConfig = kafkaConfig.getConfig("producer")

      val bootstrapServers = producerConfig.getString("bootstrap.servers")
      val acks = producerConfig.getString("acks")
      val schemaRegistryUrl = producerConfig.getString("schema.registry.url")
      val zookeeperConnect = producerConfig.getString("zookeeper.connect")
    }

    object Consumer {

      val consumerConfig = kafkaConfig.getConfig("consumer")

      val bootstrapServers = consumerConfig.getString("bootstrap.servers")
      val zookeeperConnect = consumerConfig.getString("zookeeper.connect")
      val autoCommit = consumerConfig.getBoolean("enable.auto.commit")
      val autoOffset = consumerConfig.getString("auto.offset.reset")
      val schemaRegistryUrl = consumerConfig.getString("schema.registry.url")
      val fetchMaxBytes = consumerConfig.getString("max.partition.fetch.bytes")
      val scheduleInterval = consumerConfig.getInt("schedule.interval")
      val unconfirmedTimeout = consumerConfig.getInt("unconfirmed.timeout")

    }

  }

  object Http {
    private val httpConfig = config.getConfig("http")
    val host: String = httpConfig.getString("host")
    val port: Int = httpConfig.getInt("port")
  }
}

object Settings extends ExtensionId[SettingsImpl] {
  override def createExtension(system: ExtendedActorSystem): SettingsImpl = new SettingsImpl(system.settings.config)
}
