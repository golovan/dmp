package pro.faber

import org.apache.hadoop.io.compress.GzipCodec
import org.apache.spark.SparkContext
import pro.faber.infrastructure.AppConf

object S3Packer extends App with AppConf {

  val sc = new SparkContext(sparkMaster, "S3Packer")

  setupLogging()

  sc
    .textFile(s"$amazonS3/tweets/tweets_*/part-*")
    .repartition(1)
    .saveAsTextFile(s"$amazonS3/tweets/tweets_pack_${System.currentTimeMillis().toString}", classOf[GzipCodec])
}