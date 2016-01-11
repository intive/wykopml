organization in ThisBuild := "com.blstream.wykopml"

cancelable in Global := true

val sharedSettings: Seq[Def.Setting[_]] = Defaults.coreDefaultSettings ++ Seq(
  scalaVersion := "2.11.7",
  fork := true,
  fork in Test := true,
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
  ),
  libraryDependencies ++= Dependencies.common,
  resolvers += "websudos" at "http://dl.bintray.com/websudos/oss-releases", {
    import com.typesafe.sbt.SbtScalariform
    import com.typesafe.sbt.SbtScalariform.ScalariformKeys
    import scalariform.formatter.preferences._
    ScalariformKeys.preferences := ScalariformKeys.preferences.value
      .setPreference(AlignSingleLineCaseStatements, true)
      .setPreference(SpacesAroundMultiImports, false)
      .setPreference(DoubleIndentClassDeclaration, true)
  }
)

lazy val common = Project(id = "common", base = file("common"))
  .settings(sharedSettings)

lazy val updater = Project(id = "updater", base = file("updater"))
  .settings(sharedSettings)
  .settings(
    libraryDependencies ++= Dependencies.updater
  ).dependsOn(common)

lazy val prediction = Project(id = "prediction", base = file("prediction"))
  .settings(sharedSettings)
  .settings(
    libraryDependencies ++= Dependencies.prediction
  ).dependsOn(common)

addCommandAlias("formatAll", ";scalariformFormat;test:scalariformFormat")