name := """play-event-sourcing-starter"""

val commonSettings = Seq(
  organization := "com.appliedscala.seeds.es",
  version := "1.0-SNAPSHOT",
  scalaVersion := "2.12.10"
)
lazy val events = (project in file("events")).settings(
  commonSettings,
  libraryDependencies ++= Seq(
    "com.typesafe.play" %% "play-json" % "2.7.1",
    "org.scalikejdbc" %% "scalikejdbc" % "3.3.2"
  )
)
lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .aggregate(events)
  .dependsOn(events)

pipelineStages := Seq(digest)

libraryDependencies ++= Seq(
  jdbc,
  evolutions,
  "com.softwaremill.macwire" %% "macros" % "2.3.0" % "provided",
  "org.postgresql" % "postgresql" % "42.2.5",
  "org.scalikejdbc" %% "scalikejdbc" % "3.3.2",
  "org.scalikejdbc" %% "scalikejdbc-config"  % "3.3.2",
  "ch.qos.logback"  %  "logback-classic" % "1.2.3",
  "de.svenkubiak" % "jBCrypt" % "0.4.1"
)