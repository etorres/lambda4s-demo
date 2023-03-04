package smithy4s.aws

import java.net.URI

enum AwsTestConfiguration(val awsConfiguration: AwsConfiguration):
  case LocalStack
      extends AwsTestConfiguration(
        AwsConfiguration(
          AwsCredentials
            .Default("AKIAIOSFODNN7EXAMPLE", "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY", None),
          Some(AwsRegion.EU_WEST_1),
          s3Endpoint = Some(URI("http://localstack.test:4566")),
        ),
      )
