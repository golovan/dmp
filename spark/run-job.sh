#!/bin/bash
set -e

function printHelp {
  echo "How To:"
  echo "-c for main class. Default in env: SPARK_MAIN_CLASS"
  echo "-e for executor-memory. Default: SPARK_WORKER_MEMORY"
  echo "-d for driver-memory. Default: SPARK_DRIVER_MEMORY"
  echo "-o for total-executor-cores. Default: SPARK_CORES"
  echo "-f for jar file for submitting. Default: SPARK_JAR"
  echo "-h print this help and exit"
  exit
}

for ((i=1;i<=$#; i++)); do
  if [ ${!i} = "-h" ]; then
    printHelp
  fi
done

while getopts c:e:d:o:f: opts; do
  case ${opts} in
    c) SPARK_MAIN_CLASS=${OPTARG} ;;
    e) SPARK_WORKER_MEMORY=${OPTARG} ;;
    d) SPARK_DRIVER_MEMORY=${OPTARG} ;;
    o) SPARK_CORES=${OPTARG} ;;
    f) SPARK_JAR=${OPTARG} ;;
  esac
done

while ! (timeout 3 bash -c "</dev/tcp/$CASSANDRA_HOST/$CASSANDRA_PORT") &> /dev/null; do
    echo waiting for Cassandra to start...;
    sleep 3;
done;

while ! timeout 3 curl -i -s -u ${RABBITMQ_USER}:${RABBITMQ_PASSWORD} ${RABBITMQ_HOST}:15672/api/aliveness-test/%2F > /dev/null; do
    echo waiting for RabbitMQ to start...;
    sleep 3;
done;

/opt/spark/bin/spark-submit \
  --conf "spark.scheduler.mode=FAIR" \
  --class pro.faber.${SPARK_MAIN_CLASS} \
  --master ${SPARK_MASTER} \
  --executor-memory ${SPARK_WORKER_MEMORY} \
  --executor-cores ${SPARK_CORES} \
  --total-executor-cores ${SPARK_TOTAL_CORES} \
  --conf "spark.dynamicAllocation.enabled=false" \
  --driver-memory ${SPARK_DRIVER_MEMORY} \
  --driver-java-options=" \
    -Dapplication.cassandra.host=${CASSANDRA_HOST} \
    -Dapplication.cassandra.username=${CASSANDRA_USERNAME} \
    -Dapplication.cassandra.password=${CASSANDRA_PASSWORD} \
    -Dapplication.twitter.consumer_key=${TWITTER_CONSUMER_KEY} \
    -Dapplication.twitter.consumer_secret=${TWITTER_CONSUMER_SECRET} \
    -Dapplication.twitter.access_token=${TWITTER_ACCESS_TOKEN} \
    -Dapplication.twitter.access_token_secret=${TWITTER_ACCESS_TOKEN_SECRET}" \
  "${SPARK_JAR}"
