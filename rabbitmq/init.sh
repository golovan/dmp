#!/bin/bash

echo "Creating queues.."
rabbitmqadmin import rabbitmq.config
echo "Queues created!"
