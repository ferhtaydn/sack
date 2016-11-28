import sbt._
import Keys._

import scalariform.formatter.preferences._
import com.typesafe.sbt.SbtScalariform
import de.heikoseeberger.sbtheader.license.Apache2_0

lazy val root = (project in file("."))
  .settings(name := "sack")
  .aggregate(csv, core, api, examples)

lazy val csv = (project in file("csv"))
  .settings(projectSettings: _*)
  .settings(libraryDependencies ++= Dependencies.allDependencies)
  .settings(scoverageSettings: _*)
  .settings(assemblyJarName in assembly := "csv.jar")
  .settings(mainClass in assembly := Some("com.ferhtaydn.csv.RawToAvroGenericProcessorBoot"))
  .settings(headerSettings: _*)
  .enablePlugins(AutomateHeaderPlugin)
  .dependsOn(core)

lazy val core = (project in file("core"))
  .settings(projectSettings: _*)
  .settings(libraryDependencies ++= Dependencies.allDependencies)
  .settings(scoverageSettings: _*)
  .settings(headerSettings: _*)
  .enablePlugins(AutomateHeaderPlugin)

lazy val api = (project in file("api"))
  .settings(projectSettings: _*)
  .settings(libraryDependencies ++= Dependencies.allDependencies)
  .settings(scoverageSettings: _*)
  .settings(assemblyJarName in assembly := "api.jar")
  .settings(mainClass in assembly := Some("com.ferhtaydn.http.WebServer"))
  .settings(headerSettings: _*)
  .enablePlugins(AutomateHeaderPlugin)
  .dependsOn(core)

lazy val examples = (project in file("examples"))
  .settings(projectSettings: _*)
  .settings(libraryDependencies ++= Dependencies.allDependencies)
  .settings(scoverageSettings: _*)
  .settings(headerSettings: _*)
  .enablePlugins(AutomateHeaderPlugin)
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

lazy val headerSettings = Seq(
  // sbt createHeaders
  licenses     += ("Apache-2.0", url("http://opensource.org/licenses/apache2.0.php")),
  headers      := Map(
    "scala" -> Apache2_0("2016", "Ferhat Aydın"),
    "conf"  -> Apache2_0("2016", "Ferhat Aydın", "#")
  )
)

SbtScalariform.autoImport.scalariformPreferences := SbtScalariform.autoImport.scalariformPreferences.value
  .setPreference(AlignSingleLineCaseStatements.MaxArrowIndent, 100)
  .setPreference(AlignSingleLineCaseStatements, true)
  .setPreference(DoubleIndentClassDeclaration, true)
  .setPreference(RewriteArrowSymbols, true)