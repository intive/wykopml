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
  val akkaVersion = "2.4.1"
  Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
    "com.typesafe.akka" %% "akka-contrib" % akkaVersion
  )
}

