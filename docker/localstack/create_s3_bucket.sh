#!/usr/bin/env bash

BUCKET_NAME="test-bucket"

if [ $# -eq 0 ]; then
  awslocal s3 mb "s3://$BUCKET_NAME"
  echo "hello world" | awslocal s3 cp - "s3://$BUCKET_NAME/test-object-key"
else
  ENDPOINT_URL=$1
  aws --endpoint-url="$ENDPOINT_URL" s3 mb "s3://$BUCKET_NAME"
  echo "hello world" | aws --endpoint-url="$ENDPOINT_URL" s3 cp - "s3://$BUCKET_NAME/test-object-key"
fi
