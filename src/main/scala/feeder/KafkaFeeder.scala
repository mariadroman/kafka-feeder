package feeder

import java.util.HashMap
import java.io.File
import scala.io.Source

import org.apache.kafka.clients.producer.{ProducerConfig, KafkaProducer, ProducerRecord}

object KafkaFeeder {

  def main(args: Array[String]) {
    if (args.length < 4) {
      System.err.println("Usage: KafkaFeeder <brokerList> <topic> <githubDataDirectory> <intervalBetweenFiles>")
      System.exit(1)
    }
    val Array(brokers, topic, githubDataDirectory, intervalBetweenFiles) = args
    val producer = constructProducer(brokers)

    // Loop through json files in directory
    new File(githubDataDirectory).listFiles().toSeq.foreach{ file =>
      println(s"Processing file ${file.getName}")
      // Process them line by line. Each line corresponds to a single event
      for (line <- Source.fromFile(file, "utf-8").getLines()) {
        println(s"Pushing event to topic '$topic'")
        // Construct record and feed it into kafka.
        val event = new ProducerRecord[String, String](topic, null, line)
        producer.send(event)
      }

      println(s"Processed file ${file.getName}, sleeping for $intervalBetweenFiles milliseconds")
      Thread.sleep(intervalBetweenFiles.toLong)
    }
  }

  def constructProducer(brokers: String) : KafkaProducer[String,String] = {
    val props = new HashMap[String, Object]()
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers)
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringSerializer")
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringSerializer")
    new KafkaProducer[String, String](props)
  }
}
