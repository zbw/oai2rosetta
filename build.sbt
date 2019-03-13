name := """DspaceSubApp"""

version := "1.0"

lazy val root = (project in file(".")).enablePlugins(PlayJava, PlayEbean, JavaServerAppPackaging)

scalaVersion := "2.11.11"
routesGenerator := StaticRoutesGenerator
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






