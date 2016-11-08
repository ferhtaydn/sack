package com.ferhtaydn.sack.settings

import akka.actor.{ ExtendedActorSystem, Extension, ExtensionId }
import com.typesafe.config.Config

class SettingsImpl(config: Config) extends Extension {

  object Kafka {
    private val kafkaConfig = config.getConfig("kafka")

    object Producer {
      val producerConfig = kafkaConfig.getConfig("producer-avro")

      val bootstrapServers = producerConfig.getString("bootstrap.servers")
      val acks = producerConfig.getString("acks")
      val keySerializer = producerConfig.getString("key.serializer")
      val valueSerializer = producerConfig.getString("value.serializer")
      val schemaRegistryUrl = producerConfig.getString("schema.registry.url")
      val zookeeperConnect = producerConfig.getString("zookeeper.connect")
    }

    object Consumer {
      val consumerConfig = kafkaConfig.getConfig("consumer-avro")

      val bootstrapServers = consumerConfig.getString("bootstrap.servers")
      val zookeeperConnect = consumerConfig.getString("zookeeper.connect")
      val groupId = consumerConfig.getString("group.id")
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
