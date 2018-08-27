package pro.faber

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import io.scalac.amqp.{Connection, Message}
import org.json4s._
import org.json4s.jackson.Serialization.write
import org.json4s.native.JsonMethods.parse
import pro.faber.domain.Text
import pro.faber.infrastructure.AppConf

/** Keep data in Cassandra, and send tweet text status for analytics */
object Loader extends App with AppConf {

  // Get rid of log spam (should be called after the context is set up)
  setupLogging()
  //TODO: Enable logger for akka

  // streaming from Accumulator to Formalizer
  val connection = Connection(appConfig)

  // create org.reactivestreams.Publisher
  val queue = connection.consume(queue = amqpQueueTransform)

  // create org.reactivestreams.Subscriber
  val exchange = connection.publish(exchange = amqpExchange, routingKey = amqpQueueLoad)

  implicit val system: ActorSystem = ActorSystem()
  implicit val mat: ActorMaterializer = ActorMaterializer()

  // Run akka-streams with queue as Source and exchange as Sink
  Source.fromPublisher(queue)
    .map(s => Keep(s.message))
    .runWith(Sink.fromSubscriber(exchange))

  def Keep(message: Message): Message = {
    val json = new String(message.body.toArray, "UTF-8")

    // TODO: Save to Cassandra...  ;=)

    val text = parse(json).extract[Text]
    Message(write(text).getBytes().toIndexedSeq)
  }

}