package com.ferhtaydn.common.settings

import akka.actor.ActorSystem
import akka.testkit.TestKit
import org.scalatest.{ BeforeAndAfterAll, Matchers, WordSpecLike }

class SettingsTest
    extends TestKit(ActorSystem("SettingsTest"))
    with WordSpecLike
    with BeforeAndAfterAll
    with Matchers {

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "SettingsExtension" when {

    val settings = Settings(system)

    "Kafka Producer configs" should {

      val kafkaProducer = settings.Kafka.Producer

      "return correct values" in {

        kafkaProducer.schemaRegistryUrl shouldBe "http://localhost:8081"
        kafkaProducer.bootstrapServers shouldBe "localhost:9092"
        kafkaProducer.acks shouldBe "all"
        kafkaProducer.zookeeperConnect shouldBe "localhost:2181"
      }
    }

    "Kafka Consumer configs" should {

      val kafkaConsumer = settings.Kafka.Consumer

      "return correct values" in {

        kafkaConsumer.schemaRegistryUrl shouldBe "http://localhost:8081"
        kafkaConsumer.bootstrapServers shouldBe "localhost:9092"
        kafkaConsumer.zookeeperConnect shouldBe "localhost:2181"
        kafkaConsumer.autoCommit shouldBe false
        kafkaConsumer.autoOffset shouldBe "earliest"
        kafkaConsumer.fetchMaxBytes shouldBe "1048576"
        kafkaConsumer.scheduleInterval shouldBe 1000
        kafkaConsumer.unconfirmedTimeout shouldBe 3000

      }
    }

    "Http configs" should {

      val http = settings.Http

      "return correct values" in {
        http.host shouldBe "localhost"
        http.port shouldBe 8080
      }
    }

  }

}