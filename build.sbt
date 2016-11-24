import sbt._
import Keys._

import scalariform.formatter.preferences._
import com.typesafe.sbt.SbtScalariform
import com.typesafe.sbt.SbtScalariform.ScalariformKeys

lazy val root = (project in file("."))
  .settings(name := "sack")
  .aggregate(csv, core, api, examples)

lazy val csv = (project in file("csv"))
  .settings(projectSettings: _*)
  .settings(libraryDependencies ++= Dependencies.allDependencies)
  .settings(scoverageSettings: _*)
  .dependsOn(core)

lazy val core = (project in file("core"))
  .settings(projectSettings: _*)
  .settings(libraryDependencies ++= Dependencies.allDependencies)
  .settings(scoverageSettings: _*)

lazy val api = (project in file("api"))
  .settings(projectSettings: _*)
  .settings(libraryDependencies ++= Dependencies.allDependencies)
  .settings(scoverageSettings: _*)
  .dependsOn(core)

lazy val examples = (project in file("examples"))
  .settings(projectSettings: _*)
  .settings(libraryDependencies ++= Dependencies.allDependencies)
  .settings(scoverageSettings: _*)
  .dependsOn(core)

lazy val projectSettings = Seq(
  version := "0.2.0",
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
  autoAPIMappings := true,
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