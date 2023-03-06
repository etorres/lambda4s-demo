package es.eriktorr.lambda4s
package aws

import infrastructure.LocalStackProxy.{LocalStackHttp4sProxy, LocalStackSttpProxy}

import cats.effect.{IO, Resource}
import munit.CatsEffectSuite
import org.http4s.client.middleware.Logger as Http4sLogger
import org.http4s.ember.client.EmberClientBuilder
import smithy4s.aws.{AwsClientError, AwsTestConfiguration}
import sttp.client3.impl.cats.FetchCatsBackend
import sttp.client3.{basicRequest, UriContext}

final class S3ObjectsSuite extends CatsEffectSuite:
  test("should check if an object key exist in a bucket using http4s".ignore) {
    EmberClientBuilder
      .default[IO]
      .build
      .map(Http4sLogger(logHeaders = true, logBody = true)(_))
      .use(httpClient =>
        S3Objects
          .impl(awsConfiguration, LocalStackHttp4sProxy.impl(httpClient, testBucket))
          .exists(testBucket, testObjectKey),
      )
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
                  .send(LocalStackSttpProxy.impl(backend, testBucket))
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
          .exists(testBucket, testObjectKey),
      )
      .assertEquals(true)
  }

  private lazy val awsConfiguration = AwsTestConfiguration.LocalStack.awsConfiguration

  private lazy val (testBucket, testObjectKey) = ("test-bucket", "test-object-key")
