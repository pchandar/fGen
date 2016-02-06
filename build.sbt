name := "fGen"

version := "1.0"

scalaVersion := "2.11.7"

organization := "com.pchandar"

// If you need to specify main classes manually, use packSettings and packMain
packSettings

packMain := Map("fGen" -> "com.pchandar.app.Boot")
packJarNameConvention := s"${name.value}-${version.value}"
packGenerateWindowsBatFile := true

unmanagedJars in Compile <++= baseDirectory map { base =>
    val libs = base / "lib"
    (libs ** "*.jar").classpath
}

checksums in update := Nil

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
  "de.jflex" % "jflex" % "1.6.1" % "provided",

  // NLP libraries
  "edu.stanford.nlp" % "stanford-corenlp" % "3.5.1",

  // Fast HashMap
  "net.openhft" % "chronicle-map" % "2.4.12"

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
