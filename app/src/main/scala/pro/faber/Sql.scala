package pro.faber

import org.apache.hadoop.io.compress.GzipCodec
import org.apache.spark.SparkContext
import pro.faber.infrastructure.AppConf

object Sql extends App with AppConf {

  val sc = new SparkContext(sparkMaster, "Sql")

  setupLogging()

}