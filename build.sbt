name := """DspaceSubApp"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava, PlayEbean)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  javaJdbc,
  cache,
  javaWs,
  evolutions,
  "mysql" % "mysql-connector-java" % "5.1.21",
  "commons-net" % "commons-net" % "3.3",
  "commons-lang" % "commons-lang" % "2.3",
  "dom4j" % "dom4j" % "1.6",
  "com.jcraft" % "jsch" % "0.1.55"
)






