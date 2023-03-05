package es.eriktorr.lambda4s
package aws

import cats.effect.{IO, Resource}
import munit.CatsEffectSuite
import org.http4s.client.middleware.Logger as Http4sLogger
import org.http4s.ember.client.EmberClientBuilder
import smithy4s.aws.{AwsClientError, AwsTestConfiguration, S3AccessStyle}
import sttp.client3.{basicRequest, UriContext}
import sttp.client3.impl.cats.FetchCatsBackend

final class S3ObjectsSuite extends CatsEffectSuite:
  test("should check if an object key exist in a bucket using http4s".ignore) {
    EmberClientBuilder
      .default[IO]
      .build
      .map(Http4sLogger(logHeaders = true, logBody = true)(_))
      .use(S3Objects.impl(awsConfiguration, _).exists("test-bucket", "test-object-key"))
      .assertEquals(true)
  }

  test("should check if an object key exist in a bucket using sttp") {
    Resource
      .make(IO.delay(FetchCatsBackend[IO]()))(_.close())
      .use(backend =>
        S3Objects
          .impl(
            awsConfiguration,
            request =>
              for
                response <- basicRequest
                  .get(uri"${request.uri}")
                  .headers(request.headers.headers.map(x => x.name.toString -> x.value).toMap)
                  .send(backend)
                body <- response.body
                  .fold(
                    error =>
                      IO.raiseError(
                        AwsClientError(response.code.code, error.getBytes("UTF-8").nn),
                      ),
                    IO.pure,
                  )
              yield body,
          )
          .exists("test-bucket", "test-object-key"),
      )
      .assertEquals(true)
  }

  private lazy val awsConfiguration = AwsTestConfiguration.LocalStack.awsConfiguration
