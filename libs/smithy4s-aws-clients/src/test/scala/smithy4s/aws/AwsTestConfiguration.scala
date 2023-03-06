package smithy4s.aws

enum AwsTestConfiguration(val awsConfiguration: AwsConfiguration):
  case LocalStack
      extends AwsTestConfiguration(
        AwsConfiguration(
          AwsCredentials
            .Default("AKIAIOSFODNN7EXAMPLE", "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY", None),
          Some(AwsRegion.EU_WEST_1),
        ),
      )
