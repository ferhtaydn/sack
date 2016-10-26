import scalariform.formatter.preferences._
import com.typesafe.sbt.SbtScalariform
import com.typesafe.sbt.SbtScalariform.ScalariformKeys

name := "sack"

organization := "com.ferhtaydn"

version := "0.1.0"

scalaVersion := "2.11.8"

crossScalaVersions := Seq("2.10.6", "2.11.8")

resolvers ++= Seq(
  "Sonatype OSS Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/",
  "confluent" at "http://packages.confluent.io/maven/"
)

resolvers += Resolver.bintrayRepo("cakesolutions", "maven")

libraryDependencies ++= Seq(
  "net.cakesolutions" %% "scala-kafka-client" % "0.10.0.0"
    exclude("log4j", "log4j")
    exclude("org.slf4j", "slf4j-log4j12")
    exclude("org.slf4j", "log4j-over-slf4j")
    exclude("org.slf4j", "slf4j-api"),

  "net.cakesolutions" %% "scala-kafka-client-akka" % "0.10.0.0"
    exclude("log4j", "log4j")
    exclude("org.slf4j", "slf4j-log4j12")
    exclude("org.slf4j", "log4j-over-slf4j")
    exclude("org.slf4j", "slf4j-api"),

  "net.cakesolutions" %% "scala-kafka-client-testkit" % "0.10.0.0" % "test",

  "com.sksamuel.avro4s" %% "avro4s-core" % "1.6.1",
  "io.confluent" % "kafka-avro-serializer" % "3.0.1",

  "org.slf4j" % "slf4j-api" % "1.7.21",
  "org.slf4j" % "log4j-over-slf4j" % "1.7.21",

  "org.apache.kafka" % "kafka_2.11" % "0.10.0.1"
    exclude("log4j", "log4j")
    exclude("org.slf4j", "slf4j-log4j12")
    exclude("org.slf4j", "log4j-over-slf4j")
    exclude("org.slf4j", "slf4j-api"),

  "org.scalatest" %% "scalatest" % "3.0.0" % "test",
  "org.scalacheck" %% "scalacheck" % "1.13.3" % "test"
)

scalacOptions ++= Seq(
    "-target:jvm-1.8",
    "-deprecation",
    "-encoding", "UTF-8",
    "-feature",
    "-language:existentials",
    "-language:higherKinds",
    "-language:implicitConversions",
    "-language:experimental.macros",
    "-unchecked",
    //"-Ywarn-unused-import",
    "-Ywarn-nullary-unit",
    "-Xfatal-warnings",
    "-Xlint",
    "-Yinline-warnings",
    "-Ywarn-dead-code",
    "-Xfuture")

initialCommands := "import com.ferhtaydn.sack._"

SbtScalariform.scalariformSettings

ScalariformKeys.preferences := ScalariformKeys.preferences.value
  .setPreference(AlignSingleLineCaseStatements, true)
  .setPreference(DoubleIndentClassDeclaration, true)
  .setPreference(RewriteArrowSymbols, true)