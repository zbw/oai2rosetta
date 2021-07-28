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
  "commons-io" % "commons-io" % "2.4",
  "jaxen" % "jaxen" % "1.1.6",
  "net.sourceforge.jexcelapi" % "jxl" % "2.6.12",
  "log4j" % "log4j" % "1.2.14",
  "xml-resolver" % "xml-resolver" % "1.1",
  "org.apache.xmlbeans" % "xmlbeans" % "2.3.0",
  "org.apache.xmlbeans" % "xmlbeans-xpath" % "2.3.0",
  "xalan" % "xalan" % "2.7.0",
  "xerces" % "xercesImpl" % "2.11.0",
  "dom4j" % "dom4j" % "1.6",
  "com.jcraft" % "jsch" % "0.1.55",
  "org.mockito" % "mockito-core" % "2.7.19" % Test
)

dockerEntrypoint := Seq(
  "bin/oai2rosetta",
  "-Dconfig.resource=docker.conf")
