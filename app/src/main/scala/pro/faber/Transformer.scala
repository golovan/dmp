package pro.faber

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import io.scalac.amqp.{Connection, Message}
import org.json4s._
import org.json4s.jackson.Serialization.write
import org.json4s.native.JsonMethods.parse
import pro.faber.domain.Tweet
import pro.faber.infrastructure.AppConf

import scala.util.control.Breaks._

/** Formalize and enrich json data */
object Transformer extends App with AppConf {

  // Get rid of log spam (should be called after the context is set up)
  setupLogging()
  //TODO: Enable logger for akka

  // streaming from Accumulator to Formalizer
  val connection = Connection(appConfig)

  // create org.reactivestreams.Publisher
  val queue = connection.consume(queue = amqpQueueExtract)

  // create org.reactivestreams.Subscriber
  val exchange = connection.publish(exchange = amqpExchange, routingKey = amqpQueueTransform)

  implicit val system: ActorSystem = ActorSystem()
  implicit val mat: ActorMaterializer = ActorMaterializer()

  // Run akka-streams with queue as Source and exchange as Sink
  Source.fromPublisher(queue)
    .map(s => Enrich(Formalize(s.message)))
    .runWith(Sink.fromSubscriber(exchange))

  def Formalize(message: Message): Tweet = {
    val json = new String(message.body.toArray, "UTF-8")
    val tweet = parse(json).extract[Tweet]

    // TODO: Refactor this
    // TODO: Create new object type for formalized object
    // TODO: Convert createdAt from string to Date
    tweet
  }

  def Enrich(tweet: Tweet): Message = {
    // TODO: Refactor this

    // Enrich with Device
    val enrichedTweet = tweet.copy(device = Option(enrichDevice(tweet.source)))

    Message(write(enrichedTweet).getBytes().toIndexedSeq)
  }

  def enrichDevice(source: String): String = {
    var ret: String = ""

    breakable { for (condition <- Set("android", "iphone", "web")) {
      if (source.toLowerCase.contains(condition)) {
        ret = condition
        break
      }
    }}

    // if (ret.isEmpty) println(source)
    // TODO: Add more devices

    ret
  }

}