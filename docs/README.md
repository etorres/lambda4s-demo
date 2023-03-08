# Lambda4s Demo Guide

AWS Lambda with Scala.js demo.

Testing against a LocalStack instance uses the middleware approach described in: [Using Smithy4s with LocalStack](https://disneystreaming.github.io/smithy4s/docs/protocols/aws/localstack). In this way, we avoid introducing additional parameters to configure a dedicated S3 endpoint for LocalStack, and also we don't need path-style requests. All this reduces the complexity of the `S3Signer` class.

The [AwsSigner](https://github.com/disneystreaming/smithy4s/blob/series/0.17/modules/aws/src/smithy4s/aws/internals/AwsSigner.scala) provided by Smithy4s lacks support for S3, and it's not a suitable replacement for our custom `S3Signer`.

## Build distribution packages

Package the project in a distribution Zip using the following command:

```shell
./scripts/package.sh -app operations,notifications
```

See also: [Deploy Node.js Lambda functions with .zip file archives](https://docs.aws.amazon.com/lambda/latest/dg/nodejs-package.html).

## Deploy to AWS

Deploy the project to AWS with the following commands:

```shell
serverless deploy --aws-s3-accelerate -c serverless-operations.yml

serverless deploy --aws-s3-accelerate -c serverless-notifications.yml
```

## Removing from AWS

Remove all the infrastructure from AWS with the following command:

```shell
serverless remove -c serverless-notifications.yml

serverless remove -c serverless-operations.yml
```

For additional options see: [Deploying to AWS](https://www.serverless.com/framework/docs/providers/aws/guide/deploying).

## Testing a function locally

```shell
serverless invoke local -f exists-in-s3 -d '{"objectKey":"test"}'
```

```shell
curl -v https://{API_Gateway_Id}.execute-api.{AWS_Region}.amazonaws.com/<stage>/<function>/<params>
```

## Logs

Log groups in Amazon CloudWatch:

* Notifications: /aws/lambda/lambda4s-notifications-prd-cloudwatch-logs
* Operations: /aws/lambda/lambda4s-operations-prd-http4s

## References

* [The Sakila Database](https://www.jooq.org/sakila).