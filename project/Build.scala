import com.typesafe.sbt.SbtPgp.autoImport._
import de.johoop.jacoco4sbt.JacocoPlugin.jacoco
import sbt.Keys._
import sbt._
import xerial.sbt.Sonatype._

object Build extends sbt.Build {
  val copySharedSourceFiles = TaskKey[Unit]("copied shared services source code")

  val akkaVersion = "2.3.6"
  val kryoVersion = "0.3.2"
  val clouderaVersion = "2.6.0-cdh5.4.2"
  val clouderaHBaseVersion = "1.0.0-cdh5.4.2"
  val codahaleVersion = "3.0.2"
  val commonsHttpVersion = "3.1"
  val commonsLoggingVersion = "1.1.3"
  val commonsLangVersion = "2.6"
  val commonsIOVersion = "2.4"
  val guavaVersion = "16.0.1"
  val dataReplicationVersion = "0.7"
  val upickleVersion = "0.3.4"
  val junitVersion = "4.12"
  val kafkaVersion = "0.8.2.1"
  val stormVersion = "0.9.5"
  val slf4jVersion = "1.7.7"
  val gsCollectionsVersion = "6.2.0"
  
  val crossScalaVersionNumbers = Seq("2.10.5", "2.11.5")
  val scalaVersionNumber = crossScalaVersionNumbers.last
  val sprayVersion = "1.3.2"
  val sprayJsonVersion = "1.3.1"
  val sprayWebSocketsVersion = "0.1.4"
  val scalaTestVersion = "2.2.0"
  val scalaCheckVersion = "1.11.3"
  val mockitoVersion = "1.10.17"
  val bijectionVersion = "0.7.0"
  val scalazVersion = "7.1.1"
  val algebirdVersion = "0.9.0"
  val chillVersion = "0.6.0"

  val distDirectory = "output"

  val commonSettings = Seq(jacoco.settings:_*) ++ sonatypeSettings  ++ net.virtualvoid.sbt.graph.Plugin.graphSettings ++
    Seq(
        resolvers ++= Seq(
          "patriknw at bintray" at "http://dl.bintray.com/patriknw/maven",
          "maven-repo" at "http://repo.maven.apache.org/maven2",
          "maven1-repo" at "http://repo1.maven.org/maven2",
          "maven2-repo" at "http://mvnrepository.com/artifact",
          "sonatype" at "https://oss.sonatype.org/content/repositories/releases",
          "bintray/non" at "http://dl.bintray.com/non/maven",
          "cloudera" at "https://repository.cloudera.com/artifactory/cloudera-repos",
          "clockfly" at "http://dl.bintray.com/clockfly/maven",
          "vincent" at "http://dl.bintray.com/fvunicorn/maven",
          "clojars" at "http://clojars.org/repo"
        ),
        addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0-M5" cross CrossVersion.full)
    ) ++
    Seq(
      scalaVersion := scalaVersionNumber,
      crossScalaVersions := crossScalaVersionNumbers,
      organization := "com.github.intel-hadoop",
      useGpg := false,
      pgpSecretRing := file("./secring.asc"),
      pgpPublicRing := file("./pubring.asc"),
      scalacOptions ++= Seq("-Yclosure-elim","-Yinline"),
      publishMavenStyle := true,

      pgpPassphrase := Option(System.getenv().get("PASSPHRASE")).map(_.toArray),
      credentials += Credentials(
                   "Sonatype Nexus Repository Manager", 
                   "oss.sonatype.org", 
                   System.getenv().get("SONATYPE_USERNAME"), 
                   System.getenv().get("SONATYPE_PASSWORD")),
      
      pomIncludeRepository := { _ => false },

      publishTo := {
        val nexus = "https://oss.sonatype.org/"
        if (isSnapshot.value)
          Some("snapshots" at nexus + "content/repositories/snapshots")
        else
          Some("releases"  at nexus + "service/local/staging/deploy/maven2")
      },

      publishArtifact in Test := true,

      pomExtra := {
      <url>https://github.com/intel-hadoop/gearpump</url>
      <licenses>
        <license>
          <name>Apache 2</name>
          <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
        </license>
      </licenses>
      <scm>
        <connection>scm:git:github.com/intel-hadoop/gearpump</connection>
        <developerConnection>scm:git:git@github.com:intel-hadoop/gearpump</developerConnection>
        <url>github.com/intel-hadoop/gearpump</url>
      </scm>
      <developers>
        <developer>
          <id>gearpump</id>
          <name>Gearpump Team</name>
          <url>https://github.com/intel-hadoop/teams/gearpump</url>
        </developer>
      </developers>
    }
  )

  val coreDependencies = Seq(
        libraryDependencies ++= Seq(
        "com.github.intel-hadoop" % "gearpump-shaded-metrics-graphite" % codahaleVersion,
        "org.slf4j" % "slf4j-api" % slf4jVersion,
        "org.slf4j" % "slf4j-log4j12" % slf4jVersion,
        "com.github.intel-hadoop" % "gearpump-shaded-guava" % guavaVersion,
        "commons-lang" % "commons-lang" % commonsLangVersion,
        "com.typesafe.akka" %% "akka-actor" % akkaVersion,
        "com.typesafe.akka" %% "akka-remote" % akkaVersion,
        "com.typesafe.akka" %% "akka-agent" % akkaVersion,
        "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
        "com.typesafe.akka" %% "akka-http-experimental" % "1.0",
        "com.typesafe.akka" %% "akka-http-core-experimental" % "1.0",
        "com.typesafe.akka" %% "akka-stream-experimental" % "1.0",
        "com.typesafe.akka" %% "akka-http-spray-json-experimental"% "1.0",
        "com.typesafe.akka" %% "akka-kernel" % akkaVersion,
        "com.github.intel-hadoop" %% "gearpump-shaded-akka-kryo" % kryoVersion,
        "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test",
        "org.scalatest" %% "scalatest" % scalaTestVersion % "test",
        "org.scalacheck" %% "scalacheck" % scalaCheckVersion % "test",
        "org.mockito" % "mockito-core" % mockitoVersion % "test",
        "junit" % "junit" % junitVersion % "test"
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