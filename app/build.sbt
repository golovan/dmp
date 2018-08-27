name := "app"

version := "1.0.0"

scalaVersion := "2.11.11"

libraryDependencies ++= {
  val sparkVersion = "2.2.1"

  Seq(
    "org.apache.spark" %% "spark-core" % sparkVersion,
    "org.apache.spark" %% "spark-sql" % sparkVersion,
    "org.apache.spark" %% "spark-streaming" % sparkVersion,

    "com.rabbitmq" % "amqp-client" % "3.6.6",
    "io.scalac" %% "reactive-rabbit" % "1.1.4",
    "com.stratio.receiver" % "spark-rabbitmq" % "0.5.1",

    "com.datastax.spark" %% "spark-cassandra-connector" % "2.0.7",

    "org.twitter4j" % "twitter4j-core" % "4.0.6",
    "org.twitter4j" % "twitter4j-stream" % "4.0.6",
    "org.apache.bahir" %% "spark-streaming-twitter" % "2.2.0",

    "com.typesafe.akka" %% "akka-http" % "10.0.10",
    "com.typesafe.akka" %% "akka-http-spray-json" % "10.0.10",

    "org.json4s" %% "json4s-core" % "3.5.3",
    "org.json4s" %% "json4s-native" % "3.5.3",
    "com.google.code.gson" % "gson" % "2.8.2",

    "com.typesafe.scala-logging" %% "scala-logging" % "3.8.0",
    "ch.qos.logback" % "logback-classic" % "1.3.0-alpha4",

    "junit" % "junit" % "4.12" % "test",
    "org.specs2" %% "specs2-core" % "4.0.3" % "test",
    "org.specs2" %% "specs2-junit" % "4.0.3" % "test",
    "org.scalatest" %% "scalatest" % "3.1.0-SNAP6" % "test"
  )
}

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", _ @ _*) => MergeStrategy.discard
  case PathList("reference.conf") => MergeStrategy.concat
  case _ => MergeStrategy.first
}
