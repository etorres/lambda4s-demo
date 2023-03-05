package smithy4s.aws

import smithy4s.aws.{AwsCredentials, AwsRegion}

import java.net.URI

final case class AwsConfiguration(
    credentials: AwsCredentials,
    region: Option[AwsRegion],
    s3Endpoint: Option[URI],
    s3AccessStyle: S3AccessStyle = S3AccessStyle.VirtualHostedStyle,
)
