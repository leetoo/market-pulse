name := """currency-trade-visualizer"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  "org.scalatest"       %% "scalatest"      % "2.2.1"   % "test",
  "org.scalatestplus"   %% "play"           % "1.2.0"   % "test",
  "com.typesafe.akka"   %% "akka-testkit"   % "2.3.11"  % "test",
  jdbc,
  anorm,
  cache,
  ws
)
