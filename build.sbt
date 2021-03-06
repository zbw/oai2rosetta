name := """OAI2Rosetta"""

version := "1.0"

lazy val root = (project in file(".")).enablePlugins(PlayJava, PlayEbean, JavaServerAppPackaging)


sources in (Compile, doc) := Seq.empty

publishArtifact in (Compile, packageDoc) := false

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
  "com.jcraft" % "jsch" % "0.1.55",
  "org.mockito" % "mockito-core" % "2.7.19" % Test
)

dockerEntrypoint := Seq(
  "bin/oai2rosetta",
  "-Dconfig.resource=docker.conf")
