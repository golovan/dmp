package pro.faber.infrastructure

import com.rabbitmq.client.{Channel, Connection, ConnectionFactory}
import com.typesafe.config.{Config, ConfigFactory}
import org.apache.log4j.{Level, Logger}
import org.json4s.DefaultFormats

trait AppConf {

  implicit val formats: DefaultFormats.type = DefaultFormats

  protected val appConfig: Config = ConfigFactory.load().getConfig("application")

  protected val sparkMaster: String = appConfig.getString("spark.master")

  protected val amazonS3: String = appConfig.getString("amazon.s3")

  protected val amqpFactory: ConnectionFactory = new ConnectionFactory()

  amqpFactory.setHost(appConfig.getString("amqp.host"))
  amqpFactory.setUsername(appConfig.getString("amqp.username"))
  amqpFactory.setPassword(appConfig.getString("amqp.password"))
  amqpFactory.setPort(appConfig.getInt("amqp.port"))

  protected val amqpConn: Connection = amqpFactory.newConnection
  protected val amqpChannel: Channel = amqpConn.createChannel

  protected val amqpExchange: String = appConfig.getString("amqp.exchange")
  protected val amqpQueueExtract: String = appConfig.getString("amqp.queue.extract")
  protected val amqpQueueTransform: String = appConfig.getString("amqp.queue.transform")
  protected val amqpQueueLoad: String = appConfig.getString("amqp.queue.load")

  /** Makes sure only ERROR messages get logged to avoid log spam. */
  protected def setupLogging(): Unit = {
    Logger.getRootLogger.setLevel(Level.ERROR)
  }

  /** Configures Twitter service credentials using twitter.txt in the main workspace directory */
  protected def setupTwitter(): String = {
    val twitter: Config = ConfigFactory.load().getConfig("application.twitter")
    System.setProperty("twitter4j.oauth.consumerKey", twitter.getString("consumer_key"))
    System.setProperty("twitter4j.oauth.consumerSecret", twitter.getString("consumer_secret"))
    System.setProperty("twitter4j.oauth.accessToken", twitter.getString("access_token"))
    System.setProperty("twitter4j.oauth.accessTokenSecret", twitter.getString("access_token_secret"))
  }

}
