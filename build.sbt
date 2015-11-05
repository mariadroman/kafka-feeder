lazy val root = (project in file(".")).
  settings(
    name := "kafka-feeder",
    version := "1.0",
    scalaVersion := "2.11.4",
    libraryDependencies += "org.apache.kafka" % "kafka-clients" % "0.8.2.2"
  )
