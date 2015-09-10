import com.typesafe.sbt.SbtPgp.autoImport._
import de.johoop.jacoco4sbt.JacocoPlugin.jacoco
import sbt.Keys._
import sbt._
import xerial.sbt.Sonatype._

object Build extends sbt.Build {
  val copySharedSourceFiles = TaskKey[Unit]("copied shared services source code")

  val akkaVersion = "2.3.6"
  val slf4jVersion = "1.7.7"
  val crossScalaVersionNumbers = Seq("2.10.5", "2.11.5")
  val scalaVersionNumber = crossScalaVersionNumbers.last

  val commonSettings = Seq(jacoco.settings:_*) ++ sonatypeSettings  ++ net.virtualvoid.sbt.graph.Plugin.graphSettings ++
    Seq(
      resolvers ++= Seq(
        "patriknw at bintray" at "http://dl.bintray.com/patriknw/maven",
        "maven-repo" at "http://repo.maven.apache.org/maven2",
        "maven1-repo" at "http://repo1.maven.org/maven2",
        "maven2-repo" at "http://mvnrepository.com/artifact",
        "sonatype" at "https://oss.sonatype.org/content/repositories/releases",
        "bintray/non" at "http://dl.bintray.com/non/maven"
      ),
      addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0-M5" cross CrossVersion.full)
    )

  val coreDependencies = Seq(
    libraryDependencies ++= Seq(
      "org.slf4j" % "slf4j-api" % slf4jVersion,
      "org.slf4j" % "slf4j-log4j12" % slf4jVersion,
      "com.typesafe.akka" %% "akka-actor" % akkaVersion,
      "com.typesafe.akka" %% "akka-remote" % akkaVersion,
      "com.typesafe.akka" %% "akka-agent" % akkaVersion,
      "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
      "com.typesafe.akka" %% "akka-http-experimental" % "1.0",
      "com.typesafe.akka" %% "akka-http-core-experimental" % "1.0",
      "com.typesafe.akka" %% "akka-stream-experimental" % "1.0",
      "com.typesafe.akka" %% "akka-http-spray-json-experimental"% "1.0",
      "com.typesafe.akka" %% "akka-kernel" % akkaVersion
    ),
    libraryDependencies <+= (scalaVersion)("org.scala-lang" % "scala-reflect" % _),
    libraryDependencies ++= (
      if (scalaVersion.value.startsWith("2.10")) List("org.scalamacros" %% "quasiquotes" % "2.1.0-M5")
      else List("org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.3")
      )
  )

  lazy val root = Project(
    id = "akka-http-file-server",
    base = file("."),
    settings = commonSettings ++ coreDependencies
  )
}