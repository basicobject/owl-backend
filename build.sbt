ThisBuild / name := "OwlMessenger"

ThisBuild / organization := "io.github.basicobject"

ThisBuild / version := "0.0.1"

ThisBuild / scalaVersion := "2.13.3"

val AkkaVersion = "2.6.9"
val CirceVersion = "0.12.3"

lazy val messagingService =
  (project in file("messaging"))
    .settings(
      name := "owl-messaging-service",
      libraryDependencies ++= Seq(
        "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
        "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
        "com.typesafe.akka" %% "akka-http" % "10.2.0",
        "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
        "ch.qos.logback" % "logback-classic" % "1.2.3",
        "io.circe" %% "circe-core" % CirceVersion,
        "io.circe" %% "circe-generic" % CirceVersion,
        "io.circe" %% "circe-parser" % CirceVersion
      )
    )
