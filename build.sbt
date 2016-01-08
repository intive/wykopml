name := "wykopml"
version := "0.1.0"
organization := "com.blstream.wykopml"

scalaVersion := "2.11.7"

fork := true

fork in Test := true

cancelable in Global := true

scalacOptions ++= Seq(
  "-language:postfixOps",
  "-language:implicitConversions",
  "-language:reflectiveCalls",
  "-language:higherKinds",
  "-language:existentials",
  "-Yinline-warnings",
  "-Xlint",
  "-deprecation",
  "-feature",
  "-unchecked",
  "-encoding", "utf8"
)

libraryDependencies ++= Seq(
  "net.ruippeixotog" %% "scala-scraper" % "0.1.2",
  "com.github.scopt" %% "scopt" % "3.3.0",
  "ch.qos.logback" % "logback-classic" % "1.1.3",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test"
)

libraryDependencies ++= {
  val ammoniteVersion = "0.5.1"
  Seq(
    "com.lihaoyi" % "ammonite-repl" % "0.5.1" cross CrossVersion.full,
    "com.lihaoyi" % "ammonite-sshd" % ammoniteVersion cross CrossVersion.full
  )
}

resolvers += "websudos" at "http://dl.bintray.com/websudos/oss-releases"
libraryDependencies ++= {
  val phantomVersion = "1.20.1"
  Seq(
    "com.websudos" %% "phantom-dsl" % phantomVersion,
    "com.websudos" %% "phantom-udt" % phantomVersion,
    "com.websudos" %% "phantom-reactivestreams" % phantomVersion
  )
}

libraryDependencies ++= {
  val akkaVersion = "2.4.1"
  Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
    "com.typesafe.akka" %% "akka-contrib" % akkaVersion
  )
}

libraryDependencies ++= {
  val akkaStreamsVersion = "2.0.1"
  Seq(
    "com.typesafe.akka" %% "akka-stream-experimental" % akkaStreamsVersion,
    "com.typesafe.akka" %% "akka-http-core-experimental" % akkaStreamsVersion,
    "com.typesafe.akka" %% "akka-http-experimental" % akkaStreamsVersion,
    "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaStreamsVersion
  )
}

import com.typesafe.sbt.SbtScalariform
import com.typesafe.sbt.SbtScalariform.ScalariformKeys
import scalariform.formatter.preferences._

SbtScalariform.defaultScalariformSettings

ScalariformKeys.preferences := ScalariformKeys.preferences.value
  .setPreference(AlignSingleLineCaseStatements, true)
  .setPreference(SpacesAroundMultiImports, false)
  .setPreference(DoubleIndentClassDeclaration, true)

addCommandAlias("formatAll", ";scalariformFormat;test:scalariformFormat")