package pro.faber

import com.rabbitmq.client._
import org.apache.spark.SparkContext
import pro.faber.infrastructure.AppConf

/** Listen to a stream of Tweets and collect them out */
object S3Loader extends App with AppConf {

  // Set up a Spark context named "S3Loader" that runs locally using all CPU cores
  val sc = new SparkContext(sparkMaster, "S3Loader")

  amqpChannel.queueBind(amqpQueueExtract, amqpExchange, amqpQueueExtract)

  // Get rid of log spam (should be called after the context is set up)
  setupLogging()

  // Create a DStream from Twitter using our streaming context
  val tweetsJson = sc.textFile(s"$amazonS3/tweets/tweets_*/part-*")

  tweetsJson.foreach(tweet => {
    amqpChannel.basicPublish(
      amqpExchange,
      amqpQueueExtract,
      MessageProperties.PERSISTENT_TEXT_PLAIN,
      tweet.getBytes())
  })

  // Close rabbitmq
  amqpChannel.close()
  amqpConn.close()
}