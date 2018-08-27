package pro.faber

import com.google.gson.Gson
import com.rabbitmq.client._
import org.apache.hadoop.io.compress.GzipCodec
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming._
import org.apache.spark.streaming.twitter._
import pro.faber.infrastructure.AppConf

/** Listen to a stream of Tweets and collect them out */
object Extractor extends App with AppConf {

  var numTweetsCollected = 0L

  val gson = new Gson()

  var unionRDD: RDD[String] = _

  // Configure Twitter credentials using twitter.txt
  setupTwitter()

  // Set up a Spark streaming context named "Accumulator" that runs locally using
  // all CPU cores and five-minutes batches of data
  val ssc = new StreamingContext(sparkMaster, "Accumulator", Seconds(1))

  amqpChannel.queueBind(amqpQueueExtract, amqpExchange, amqpQueueExtract)

  // Get rid of log spam (should be called after the context is set up)
  setupLogging()

  // Create a DStream from Twitter using our streaming context
  val tweets = TwitterUtils.createStream(ssc, None)

  // Now extract each status update in json format into RDD's using map()
  val tweetsJson = tweets.map(gson.toJson(_))

  //TODO: Spark DStream as a Source for the Akka stream
  //https://stackoverflow.com/questions/33382895/idiomatic-way-to-use-spark-dstream-as-source-for-an-akka-stream
  //https://www.programcreek.com/scala/akka.stream.scaladsl.Source
  //https://stackoverflow.com/questions/43590101/akka-stream-source-from-function
  tweetsJson.foreachRDD((rdd, time) => {
    val count = rdd.count()
    if (count > 0) {

      val outputRDD = rdd.repartition(1).cache
      outputRDD.foreach(tweet => {
        amqpChannel.basicPublish(
          amqpExchange,
          amqpQueueExtract,
          MessageProperties.PERSISTENT_TEXT_PLAIN,
          tweet.getBytes())
      })

      // Join RDDs
      unionRDD = unionRDD match {
        case null => outputRDD
        case u => u.union(outputRDD)
      }

      // Save tweets to amazon by chunks
      // TODO: Set this as config var
      if (unionRDD.count()>=100000) {
        unionRDD
          .repartition(1)
          .saveAsTextFile(s"$amazonS3/tweets/tweets_${time.milliseconds.toString}", classOf[GzipCodec])
        unionRDD = null
      }
//
//      numTweetsCollected += count
//      println(s"cumulative: $numTweetsCollected; delta: $count")
    }
  })

  // Kick it all off
  ssc.start
  ssc.awaitTermination

  // Close rabbitmq
  amqpChannel.close()
  amqpConn.close()
}