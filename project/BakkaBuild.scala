import sbt._
import sbt.Keys._

object BakkaBuild extends Build {

  lazy val bakka = Project(
    id = "bakka",
    base = file("."),
    settings = Project.defaultSettings ++ Seq(
      name := "bakka",
      organization := "molmed",
      version := "0.1-SNAPSHOT",
      scalaVersion := "2.9.2",
      resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases",
      resolvers += "Sbt-plugin Releases" at "http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases/",
      libraryDependencies += "com.typesafe.akka" % "akka-actor" % "2.0.1"      
    ) 
    ++ seq(com.github.retronym.SbtOneJar.oneJarSettings: _*)
    ++ seq(scalacOptions ++= Seq("-deprecation", "–optimise"))
    
  )
}
