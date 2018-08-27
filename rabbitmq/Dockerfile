FROM rabbitmq

RUN apt-get update && apt-get install -y python curl

COPY rabbitmqadmin /usr/local/bin/rabbitmqadmin
COPY rabbitmq.config /rabbitmq.config
COPY init.sh /init.sh

RUN rabbitmq-plugins enable --offline rabbitmq_management rabbitmq_shovel

VOLUME /var/log/rabbitmq

EXPOSE 15671 15672
