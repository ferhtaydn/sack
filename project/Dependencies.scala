import sbt._

object Dependencies {

  val scalaV = "2.11.8"

  val resolvers = DefaultOptions.resolvers(snapshot = true) ++ Seq(
    "confluent" at "http://packages.confluent.io/maven/",
    "confluent-repository" at "http://packages.confluent.io/maven/",
    Resolver.bintrayRepo("cakesolutions", "maven"),
    Resolver.bintrayRepo("hseeberger", "maven"),
    Resolver.typesafeRepo("releases"),
    Resolver.sonatypeRepo("releases")
  )

  object ScalaKafkaClient {

    private val version = "0.10.0.0"

    val scalaKafkaClient = ("net.cakesolutions" %% "scala-kafka-client" % version)
      .exclude("log4j", "log4j")
      .exclude("org.slf4j", "slf4j-log4j12")
      .exclude("org.slf4j", "log4j-over-slf4j")
      .exclude("org.slf4j", "slf4j-api")
      .excludeAll(ExclusionRule(organization = "org.apache.kafka"))

    val scalaKafkaClientAkka = ("net.cakesolutions" %% "scala-kafka-client-akka" % version)
      .exclude("log4j", "log4j")
      .exclude("org.slf4j", "slf4j-log4j12")
      .exclude("org.slf4j", "log4j-over-slf4j")
      .exclude("org.slf4j", "slf4j-api")
      .excludeAll(ExclusionRule(organization = "org.apache.kafka"))

    val scalaKafkaClientTestKit = ("net.cakesolutions" %% "scala-kafka-client-testkit" % version % "test")
      .excludeAll(ExclusionRule(organization = "org.apache.kafka"))

  }

  object ConfluentKafka {

    val kafka = ("org.apache.kafka" % "kafka_2.11" % "0.10.0.1-cp1")
      .exclude("log4j", "log4j")
      .exclude("org.slf4j", "slf4j-log4j12")
      .exclude("org.slf4j", "log4j-over-slf4j")
      .exclude("org.slf4j", "slf4j-api")

    val kafkaAvro = "io.confluent" % "kafka-avro-serializer" % "3.0.1"
  }

  object Akka {
    private val version = "2.4.11"
    val akka = "com.typesafe.akka" %% "akka-http-experimental" % version
    val akkaTestKit = "com.typesafe.akka" %% "akka-testkit" % version % "test"
  }

  object Avro4s {
    val avro4s = ("com.sksamuel.avro4s" %% "avro4s-core" % "1.6.1")
      .excludeAll(ExclusionRule(organization = "org.apache.avro"))
  }

  object Log {

    object Slf4j {
      private val version = "1.7.21"
      val slf4j = "org.slf4j" % "slf4j-api" % version
      val log4j = "org.slf4j" % "log4j-over-slf4j" % version
    }

    val logback = "ch.qos.logback" % "logback-classic" % "1.1.7"
    val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0"
  }

  object Circe {
    val circe = "io.circe" %% "circe-generic" % "0.5.2"
    val akkaHttpCirce = "de.heikoseeberger" %% "akka-http-circe" % "1.10.1"
  }

  val scalaTest = "org.scalatest" %% "scalatest" % "3.0.0" % "test"
  val scalaCheck = "org.scalacheck" %% "scalacheck" % "1.13.3" % "test"
  val shapeless = "com.chuusai" %% "shapeless" % "2.3.2"

  val coreDependencies: Seq[ModuleID] = Seq(
    shapeless
  )

  val scalaKafkaClientDependencies: Seq[ModuleID] = Seq(
    ScalaKafkaClient.scalaKafkaClient,
    ScalaKafkaClient.scalaKafkaClientAkka,
    ScalaKafkaClient.scalaKafkaClientTestKit
  )

  val confluentKafkaDependencies: Seq[ModuleID] = Seq(
    ConfluentKafka.kafka,
    ConfluentKafka.kafkaAvro
  )

  val akkaDependencies: Seq[ModuleID] = Seq(
    Akka.akka,
    Akka.akkaTestKit
  )

  val circeDependencies: Seq[ModuleID] = Seq(
    Circe.circe,
    Circe.akkaHttpCirce
  )

  val avro4sDependencies: Seq[ModuleID] = Seq(
    Avro4s.avro4s
  )

  val testDependencies: Seq[ModuleID] = Seq(
    scalaCheck,
    scalaTest,
    ScalaKafkaClient.scalaKafkaClientTestKit
  )

  val logDependencies: Seq[ModuleID] = Seq(
    Log.Slf4j.log4j,
    Log.Slf4j.slf4j,
    Log.logback,
    Log.scalaLogging
  )

  val allDependencies = coreDependencies ++
    scalaKafkaClientDependencies ++
    confluentKafkaDependencies ++
    akkaDependencies ++
    circeDependencies ++
    avro4sDependencies ++
    testDependencies

}