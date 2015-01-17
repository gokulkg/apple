import sbt._
import sbt.Keys._
import sbt._
import Keys._
import sbtassembly._
import sbtassembly.AssemblyPlugin._
import AssemblyKeys._

object Build extends sbt.Build {

  lazy val project = Project(
    id = "apple",
    base = file("."),
    settings = Project.defaultSettings ++ Seq(
      name                  := "apple",
      organization          := "com.myweb",
      version               := "0.1-SNAPSHOT",
      scalaVersion          := "2.10.4",
      scalacOptions         := Seq("-deprecation", "-feature", "-encoding", "utf8"),
      libraryDependencies   ++= Dependencies(),
      mainClass in assembly := Some("com.myweb.apple.JettyLauncher")
    )
  )

  object Dependencies {

    object Versions {
      val scalatra = "2.3.0"
      val akka = "2.3.6"
      val phantomVersion = "1.4.4"
    }

    val compileDependencies = Seq(
      "com.typesafe.akka" %% "akka-actor" % Versions.akka,
      "org.scalatra" % "scalatra_2.10" % Versions.scalatra,
      "org.json4s" % "json4s-jackson_2.10" % "3.2.10",
      "org.scalatra" % "scalatra-json_2.10" % "2.3.0",
      "org.eclipse.jetty" % "jetty-webapp" % "9.1.5.v20140505",
      "com.typesafe" % "config" % "1.2.1",
      "us.monoid.web" % "resty" % "0.3.2",
      "mysql" % "mysql-connector-java" % "5.1.34",
      "net.debasishg" % "redisclient_2.10" % "2.13",
      "io.spray" %%  "spray-json" % "1.3.0",
      "colt" % "colt" % "1.2.0",
      "commons-httpclient" % "commons-httpclient" % "3.1",
      "com.typesafe.slick" %% "slick" % "2.1.0",
      "c3p0" % "c3p0" % "0.9.1.2",
      "rome" % "rome" % "0.9",
      "com.syncthemall" % "goose" % "2.1.25",
      "org.apache.poi" % "poi-ooxml" % "3.10.1",
      "com.ning" % "async-http-client" % "1.6.5",
      "com.websudos"  %% "phantom-dsl" % Versions.phantomVersion,
      "com.websudos"  %% "phantom-zookeeper"  % Versions.phantomVersion,
      "org.scalaz" % "scalaz-core_2.10" % "7.1.0",
      "net.databinder.dispatch" % "dispatch-core_2.10" % "0.11.2"
    )

   val testDependencies = Seq(
      "com.typesafe.akka" %% "akka-testkit" % Versions.akka % "test",
      "org.scalatest" % "scalatest_2.10" % "2.2.3" % "test",
      "com.websudos"  %% "phantom-test"                  % Versions.phantomVersion % "test",
      "com.websudos"  %% "phantom-testing"               % Versions.phantomVersion % "test",
      "org.scalamock" %% "scalamock-scalatest-support" % "3.2" % "test"
    )

    def apply(): Seq[ModuleID] = compileDependencies ++ testDependencies

  }

}
