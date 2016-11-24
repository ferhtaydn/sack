import sbt._
import Keys._

import scalariform.formatter.preferences._
import com.typesafe.sbt.SbtScalariform
import com.typesafe.sbt.SbtScalariform.ScalariformKeys

lazy val root = (project in file("."))
  .settings(projectSettings: _*)
  .settings(libraryDependencies ++= Dependencies.allDependencies)
  .settings(scoverageSettings: _*)

lazy val projectSettings = Seq(
  name := "sack",
  version := "0.1.0",
  organization := "com.ferhtaydn",
  scalaVersion := Dependencies.scalaV,
  resolvers ++= Dependencies.resolvers,
  javacOptions ++= Seq("-source", "1.8", "-target", "1.8"),
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
    "-Ywarn-nullary-unit",
    "-Xfatal-warnings",
    "-Xlint",
    "-Yinline-warnings",
    "-Ywarn-dead-code",
    "-Xfuture"),
  scalacOptions in Test ++= Seq("-Yrangepos"),
  fork in Test := false,
  parallelExecution in Test := true,
  publishArtifact in Test := false,
  cancelable in Global := true,
  coverageEnabled := true //scoverage: run "sbt coverageReport" for report.
)

lazy val scoverageSettings = Seq(
  coverageExcludedPackages := "<empty>;",
  coverageExcludedFiles := "",
  coverageMinimum := 1,
  coverageFailOnMinimum := true,
  coverageHighlighting := true
)

SbtScalariform.scalariformSettings

ScalariformKeys.preferences := ScalariformKeys.preferences.value
  .setPreference(AlignSingleLineCaseStatements, true)
  .setPreference(DoubleIndentClassDeclaration, true)
  .setPreference(RewriteArrowSymbols, true)