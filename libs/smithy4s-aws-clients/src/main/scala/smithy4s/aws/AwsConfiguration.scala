package smithy4s.aws

import java.net.URI

final case class AwsConfiguration(
    credentials: AwsCredentials,
    region: Option[AwsRegion],
    s3Endpoint: Option[URI],
)
