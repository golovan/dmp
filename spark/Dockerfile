FROM java:8-jdk-alpine

RUN apk --update add --no-cache  \
  coreutils \
  bash \        
  curl \
  tar \
  procps 

# Apache Spark version 2.2.1
ENV SPARKVERSION=spark-2.2.1-bin-hadoop2.7 \
  SPARK_WORKER_MEMORY=1024M \
  SPARK_DRIVER_MEMORY=1024M \
  SPARK_EXECUTOR_INSTANCES=1 \
  LOG_LEVEL=INFO 

COPY start.sh /start.sh
RUN chmod +x /start.sh

RUN mkdir /amazon_s3 && mkdir /opt \
  && curl https://archive.apache.org/dist/spark/spark-2.2.1/spark-2.2.1-bin-hadoop2.7.tgz | \
  # Untar Spark, in /opt default to hadoop 2.7 version, and remove extracted file
  tar -xzf - -C /opt/ \
  # Soft link to version of Spark
  && mv /opt/${SPARKVERSION} /opt/spark

ADD run-job.sh /bin
RUN chmod +x /bin/run-job.sh
RUN ln -s /bin/run-job.sh /bin/run-job

VOLUME /opt/spark/logs
