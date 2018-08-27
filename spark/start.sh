#!/bin/bash
set -xe

for className in Extractor Transformer Loader RealTimeAnalyser; do
/bin/run-job -c $className -f /amazon_s3/app.jar &
done

set +x
while true; do
sleep 10
done
