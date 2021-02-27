ThisBuild / name := "OwlMessenger"

ThisBuild / organization := "io.github.basicobject"

ThisBuild / version := "0.0.1"

ThisBuild / scalaVersion := "2.13.3"

val AkkaVersion = "2.6.9"
val AkkaHttpVersion = "10.2.0"
val CirceVersion = "0.12.3"

val commonDependencies = Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
  "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe" % "config" % "1.4.1"
)

lazy val common = project.settings(name := "owl-common")

lazy val gateway = project.settings(
  name := "owl-gateway",
  libraryDependencies ++= commonDependencies
) dependsOn common

lazy val messaging = project.settings(
  name := "owl-messaging",
  libraryDependencies ++= commonDependencies,
  libraryDependencies ++= Seq(
    "io.circe" %% "circe-core" % CirceVersion,
    "io.circe" %% "circe-generic" % CirceVersion,
    "io.circe" %% "circe-parser" % CirceVersion
  )
)

lazy val session = project
  .settings(
    name := "owl-session",
    libraryDependencies ++= commonDependencies
  )
  .dependsOn(common)
  .enablePlugins(AkkaGrpcPlugin)
