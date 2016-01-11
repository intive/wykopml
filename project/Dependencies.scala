import sbt._
import sbt.Keys._

object Dependencies {

  object Versions {
    val ammonite = "0.5.1"
    val akka = "2.4.1"
    val akkaHttp = "2.0.1"
    val phantom = "1.20.1"
    val spark = "1.6.0"
    val cassandraConnector = "1.5.0-RC1"
  }

  val ammonite = Seq(
    "com.lihaoyi" % "ammonite-repl" % Versions.ammonite cross CrossVersion.full,
    "com.lihaoyi" % "ammonite-sshd" % Versions.ammonite cross CrossVersion.full
  )

  val akka = Seq(
    "com.typesafe.akka" %% "akka-actor" % Versions.akka,
    "com.typesafe.akka" %% "akka-slf4j" % Versions.akka,
    "com.typesafe.akka" %% "akka-contrib" % Versions.akka
  )

  val akkaHttp = Seq(
    "com.typesafe.akka" %% "akka-stream-experimental" % Versions.akkaHttp,
    "com.typesafe.akka" %% "akka-http-core-experimental" % Versions.akkaHttp,
    "com.typesafe.akka" %% "akka-http-experimental" % Versions.akkaHttp,
    "com.typesafe.akka" %% "akka-http-spray-json-experimental" % Versions.akkaHttp
  )

  val phantom = Seq(
    "com.websudos" %% "phantom-dsl" % Versions.phantom,
    "com.websudos" %% "phantom-udt" % Versions.phantom,
    "com.websudos" %% "phantom-reactivestreams" % Versions.phantom
  )

  val spark = Seq(
    "org.apache.spark" %% "spark-core" % Versions.spark exclude("org.slf4j", "slf4j-log4j12"),
    "org.apache.spark" %% "spark-mllib" % Versions.spark exclude("org.slf4j", "slf4j-log4j12")
  )

  val cassandraConnector = Seq(
    "com.datastax.spark" %% "spark-cassandra-connector" % Versions.cassandraConnector exclude("org.apache.spark", "spark-core") exclude("org.slf4j", "slf4j-log4j12")
  )

  val common = Seq(
    "net.ruippeixotog" %% "scala-scraper" % "0.1.2",
    "com.github.scopt" %% "scopt" % "3.3.0",
    "ch.qos.logback" % "logback-classic" % "1.1.3",
    "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
    "org.slf4j" % "slf4j-api" % "1.7.12",
    "org.scalatest" %% "scalatest" % "2.2.4" % "test"
  )

  val updater = akka ++ akkaHttp ++ phantom

  val prediction = spark ++ cassandraConnector

}