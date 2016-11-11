package com.ferhtaydn.sack.binary

import akka.actor.{ Actor, ActorLogging, ActorRef, ActorSystem, Props }
import cakesolutions.kafka.KafkaConsumer
import cakesolutions.kafka.akka.KafkaConsumerActor.{ Confirm, Subscribe, Unsubscribe }
import cakesolutions.kafka.akka.{ ConsumerRecords, KafkaConsumerActor }
import com.ferhtaydn.sack.settings.Settings
import com.ferhtaydn.sack.{ Boot, ProductSchema }
import org.apache.kafka.common.serialization.{ ByteArrayDeserializer, StringDeserializer }

import scala.concurrent.duration._

object BinaryProductConsumerBoot extends Boot {

  val system = ActorSystem("binary-product-consumer-system")
  val settings = Settings(system)
  val consumerConfig = settings.Kafka.Consumer.consumerConfig

  val consumerConf = KafkaConsumer.Conf(
    new StringDeserializer,
    new ByteArrayDeserializer,
    groupId = "csv-binary-consumer"
  ).withConf(consumerConfig)

  val actorConf = KafkaConsumerActor.Conf(1.seconds, 3.seconds)

  system.actorOf(
    BinaryProductConsumer.props(consumerConf, actorConf),
    "binary-product-consumer-actor"
  )

  terminate(system)

}

object BinaryProductConsumer {

  def props(
    consumerConf: KafkaConsumer.Conf[String, Array[Byte]],
    actorConf: KafkaConsumerActor.Conf
  ): Props = Props(new BinaryProductConsumer(consumerConf, actorConf))
}

class BinaryProductConsumer(
    kafkaConsumerConf: KafkaConsumer.Conf[String, Array[Byte]],
    consumerActorConf: KafkaConsumerActor.Conf
) extends Actor with ActorLogging {

  val recordsExt = ConsumerRecords.extractor[String, Array[Byte]]
  val inputTopic = "product-csv-binary"

  var consumerActor: ActorRef = _

  override def preStart(): Unit = {

    super.preStart()

    consumerActor = context.actorOf(
      KafkaConsumerActor.props(kafkaConsumerConf, consumerActorConf, self),
      "kafka-consumer-actor"
    )

    context.watch(consumerActor)

    consumerActor ! Subscribe.AutoPartition(List(inputTopic))

  }

  override def postStop(): Unit = {

    consumerActor ! Unsubscribe

    context.children foreach { child ⇒
      context.unwatch(child)
      context.stop(child)
    }

    super.postStop()
  }

  override def receive: Receive = {

    case recordsExt(records) ⇒
      log.info("Records from KafkaConsumer:\n")
      processRecords(records)

  }

  private def processRecords(records: ConsumerRecords[String, Array[Byte]]) = {

    records.pairs.foreach {
      case (key, value) ⇒
        log.info(s"Received [$key, ${ProductSchema.productFromBytes(value)}]")
    }

    log.info(s"Batch complete, offsets: ${records.offsets}")
    consumerActor ! Confirm(records.offsets, commit = false)

  }
}
