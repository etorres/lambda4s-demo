package es.eriktorr.lambda4s
package aws

import cats.effect.IO
import munit.CatsEffectSuite
import org.http4s.client.middleware.Logger as Http4sLogger
import org.http4s.ember.client.EmberClientBuilder
import smithy4s.aws.AwsTestConfiguration

final class S3ObjectsSuite extends CatsEffectSuite:
  test("should check if an object key exist in a bucket") {
    EmberClientBuilder
      .default[IO]
      .build
      .map(Http4sLogger(logHeaders = true, logBody = true)(_))
      .use { httpClient =>
        val awsConfiguration = AwsTestConfiguration.LocalStack.awsConfiguration
        S3Objects
          .impl(awsConfiguration.credentials, awsConfiguration.region.get, httpClient)
          .exists("test_bucket", "test_object_key")
      }
      .assertEquals(true)
  }
