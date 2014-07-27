sbtPlugin := true

name := "sbt-handlebars"

version := "1.0"

// doesn't seem to work :((
//scalaVersion := "2.11.1"

scalaVersion := "2.10.4"

organization := "com.dataflow.sbt"

addSbtPlugin("com.typesafe.sbt" % "sbt-web" % "1.0.2")