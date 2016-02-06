name := "fGen"

version := "1.0"

scalaVersion := "2.11.7"

organization := "com.pchandar"

assemblyJarName in assembly := s"${name.value}-${version.value}"

unmanagedJars in Compile += file("lib/factorie_2.11-1.2.20160119-SNAPSHOT.jar")

assemblyOutputPath in assembly := baseDirectory.value / s"lib/${name.value}-${version.value}.jar"

mainClass in assembly := Some("com.pchandar.app.Boot")

libraryDependencies ++= Seq(
  //Mongo, JSON, XML
  "org.mongodb" %% "casbah" % "3.1.0",
  "com.typesafe.play" %% "play-json" % "2.3.0",
  "org.scala-lang.modules" %% "scala-xml" % "1.0.2",

  // Scala Test
  "org.scalatest" %% "scalatest" % "2.2.4",

  //Scalaz
  "org.scalaz" %% "scalaz-core" % "7.2.0",
  "org.scalaz" %% "scalaz-concurrent" % "7.2.0",
  "org.scalaz.stream" %% "scalaz-stream" % "0.7.1a",

  // Logging
  "ch.qos.logback" % "logback-classic" % "1.1.2",
  "org.slf4j" % "log4j-over-slf4j" % "1.7.12",

  "org.scala-lang.modules" %% "scala-async" % "0.9.2",

  // Commons
  "org.apache.commons" % "commons-lang3" % "3.4",

  // Command Line Arguments
  "org.rogach" %% "scallop" % "0.9.5",

  // JFlex
  "de.jflex" % "jflex" % "1.6.1",

  // NLP libraries
  "edu.stanford.nlp" % "stanford-corenlp" % "3.5.1",

  // Fast HashMap
  "net.openhft" % "chronicle-map" % "3.4.2-beta"

)
resolvers ++= Seq(
  "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
  Resolver.sonatypeRepo("snapshots"),
  Resolver.typesafeRepo("releases"),
  "Spray Releases" at "http://repo.spray.io",
  "Scalaz Bintray Repo" at "https://dl.bintray.com/scalaz/releases"
)

updateOptions := updateOptions.value.withCachedResolution(true)

seq(sbtjflex.SbtJFlexPlugin.jflexSettings: _*)
