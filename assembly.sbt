import sbt._
import sbt.Keys._
import sbt._
import Keys._
import sbtassembly.AssemblyPlugin._

assemblySettings

mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) => {
    case PathList("META-INF", "native", "osx", "libjansi.jnilib") => MergeStrategy.last
    case PathList("META-INF", "native", "windows32", "jansi.dll") => MergeStrategy.last
    case PathList("META-INF", "native", "windows64", "jansi.dll") => MergeStrategy.last
    case PathList("com", "twitter", "common", "args", "apt", _) => MergeStrategy.last
    case PathList("org", "hamcrest", _) => MergeStrategy.last
    case PathList("org", "hamcrest", _, _) => MergeStrategy.last
    case PathList("images", _) => MergeStrategy.last
    case PathList("javax", "servlet", _) => MergeStrategy.last
    case PathList("javax", "servlet", "http", _) => MergeStrategy.last
    case PathList("javax", "servlet", "jsp", _) => MergeStrategy.last
    case PathList("javax", "servlet", "resources", _) => MergeStrategy.last
    case PathList("javax", "servlet", "jsp", _, _) => MergeStrategy.last
    case PathList("javax", _, _, _) => MergeStrategy.last
    case PathList("javax", _, _, _, _) => MergeStrategy.last
    case PathList("org", "apache", _, _) => MergeStrategy.last
    case PathList("org", "apache", _, _, _) => MergeStrategy.last
    case PathList("org", "apache", _, _, _, _) => MergeStrategy.last
    case PathList("org", "apache", _, _, _, _, _) => MergeStrategy.last
    case PathList("org", "apache", _, _, _, _, _, _) => MergeStrategy.last
    case PathList("log4j.properties") => MergeStrategy.last
    case PathList("org", "fusesource", _, _) => MergeStrategy.last
    case PathList("org", "fusesource", _, _, _) => MergeStrategy.last
    case PathList("org", "fusesource", _, _, _, _) => MergeStrategy.last
    case PathList("com", "twitter", "common", "args", xs @ _*) => MergeStrategy.last
    case PathList("org", "objectweb", "asm", xs @ _*) => MergeStrategy.last
    case x => old(x)
  }
}

test in assembly := {}

assemblyExcludedJars in assembly := {
  val cp = (fullClasspath in assembly).value
  cp filter {x => Set("phantom-testing_2.10-1.4.4.jar", "scalatest_2.10-2.2.0-M1.jar", "phantom-test_2.10-1.4.4.jar")(x.data.getName)}
}
