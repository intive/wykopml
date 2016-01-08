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
