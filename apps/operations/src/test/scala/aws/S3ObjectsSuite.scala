package es.eriktorr.lambda4s
package aws

import cats.effect.{IO, Resource}
import munit.CatsEffectSuite
import org.http4s.client.middleware.Logger as Http4sLogger
import org.http4s.ember.client.EmberClientBuilder
import smithy4s.aws.{AwsTestConfiguration, S3AccessStyle}
import sttp.client3.impl.cats.FetchCatsBackend

final class S3ObjectsSuite extends CatsEffectSuite:
  test("should check if an object key exist in a bucket using http4s".ignore) {
    EmberClientBuilder
      .default[IO]
      .build
      .map(Http4sLogger(logHeaders = true, logBody = true)(_))
      .use(S3Objects.impl(awsConfiguration, _).exists("test_bucket", "test_object_key"))
      .assertEquals(true)
  }

  test("should check if an object key exist in a bucket using sttp") {
    Resource
      .make(IO.delay(FetchCatsBackend[IO]()))(_.close())
      .use(S3Objects.impl(awsConfiguration, _).exists("test_bucket", "test_object_key"))
      .assertEquals(true)
  }

  private lazy val awsConfiguration = AwsTestConfiguration.LocalStack.awsConfiguration
