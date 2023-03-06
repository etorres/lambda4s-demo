package smithy4s.aws

import smithy4s.aws.{AwsCredentials, AwsRegion}

final case class AwsConfiguration(
    credentials: AwsCredentials,
    region: Option[AwsRegion],
)
