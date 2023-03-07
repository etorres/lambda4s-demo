# TO-DO: list

1. Try the middleware approach described in: [Using Smithy4s with LocalStack](https://disneystreaming.github.io/smithy4s/docs/protocols/aws/localstack). This will allow us to remove the additional configuration parameters: S3AccessType and S3Endpoint, reducing the complexity of the S3Signer class.
2. Check the [AwsSigner](https://github.com/disneystreaming/smithy4s/blob/series/0.17/modules/aws/src/smithy4s/aws/internals/AwsSigner.scala) class of Smithy4s. This will allow us to replace our custom S3Signer with a call to this library.
3. Add NonEmptyString refined type to the database.