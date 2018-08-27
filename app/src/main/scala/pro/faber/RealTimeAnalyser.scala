package pro.faber

import com.rabbitmq.client.QueueingConsumer.Delivery
import org.apache.spark.streaming.rabbitmq.RabbitMQUtils
import org.apache.spark.streaming.rabbitmq.distributed.RabbitMQDistributedKey
import org.apache.spark.streaming.rabbitmq.models.ExchangeAndRouting
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.json4s._
import org.json4s.native.JsonMethods.parse
import pro.faber.domain.Text
import pro.faber.infrastructure.AppConf

/** Perform some RT Analytics */
// /bin/run-job -c RealTimeAnalyser -f /amazon_s3/spg_cs.jar
object RealTimeAnalyser extends App with AppConf {

  val ssc = new StreamingContext(sparkMaster, "RealTimeAnalyser", Seconds(5))

  // Get rid of log spam (should be called after the context is set up)
  setupLogging()

  val rabbitMqMap: scala.collection.immutable.Map[String, String] = {
    scala.collection.immutable.Map(
      "hosts" -> appConfig.getString("amqp.host"),
      "port" -> appConfig.getString("amqp.port"),
      "queueName" -> amqpQueueLoad,
      "vHost" -> appConfig.getString("amqp.virtual-host"),
      "userName" -> appConfig.getString("amqp.username"),
      "password" -> appConfig.getString("amqp.password")
    )
  }

  def messageHandler(data: Delivery): Text = {
    val json = new String(data.getBody, "UTF-8")
    val tweet = parse(json).extract[Text]
    tweet
  }

  val receiverStream = RabbitMQUtils.createStream[Text](ssc, rabbitMqMap, messageHandler)

  // Now extract the text of each status update into RDD's using map()
  val statuses = receiverStream.map(status => status.text)

  // Print out the first ten
  // statuses.print()

  // Blow out each word into a new DStream
  val tweetwords = statuses.flatMap(tweetText => tweetText.split(" "))

  // Now eliminate anything that's not a hashtag
  val hashtags = tweetwords.filter(_.startsWith("#"))

  val latin = hashtags.filter(_.matches("^.*[a-zA-Z]+.*$"))

  // Map each hashtag to a key/value pair of (hashtag, 1) so we can count them up by adding up the values
  val hashtagKeyValues = latin.map(hashtag => (hashtag, 1))

  // Now count them up over a 5 minute window sliding every one second
  val hashtagCounts = hashtagKeyValues.reduceByKeyAndWindow( (x,y) => x + y, (x,y) => x - y, Seconds(300), Seconds(5))

  // Sort the results by the count values
  val sortedResults = hashtagCounts.transform(rdd => rdd.sortBy(x => x._2, false))

  // Print the top 10
  sortedResults.print

  // TODO: Isn't it COOL?!! :)

  // Kick it all off
  ssc.checkpoint(s"$amazonS3/checkpoint/")
  ssc.start
  ssc.awaitTermination


}