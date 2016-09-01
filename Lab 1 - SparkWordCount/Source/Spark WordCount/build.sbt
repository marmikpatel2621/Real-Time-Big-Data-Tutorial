import sbt.Keys._

lazy val root = (project in file(".")).
  settings(
    name := "SparkWordCount",
    version := "1.0",
    scalaVersion := "2.11.8",
    mainClass in Compile := Some("SparkWordCount")
  )

exportJars := true
fork := true

assemblyJarName := "SparkWordCount.jar"

val meta = """META.INF(.)*""".r

assemblyMergeStrategy in assembly := {
  case PathList("javax", "servlet", xs@_*) => MergeStrategy.first
  case PathList(ps@_*) if ps.last endsWith ".html" => MergeStrategy.first
  case n if n.startsWith("reference.conf") => MergeStrategy.concat
  case n if n.endsWith(".conf") => MergeStrategy.concat
  case meta(_) => MergeStrategy.discard
  case x => MergeStrategy.first
}

// additional libraries
libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "1.6.1" % "provided",
  "org.apache.spark" %% "spark-streaming" % "1.6.1",
  "org.apache.spark" %% "spark-mllib" % "1.6.1"
)


