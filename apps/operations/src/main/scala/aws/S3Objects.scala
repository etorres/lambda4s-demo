package es.eriktorr.lambda4s
package aws

import cats.effect.IO
import org.http4s.Request
import org.http4s.client.Client
import smithy4s.aws.*
import smithy4s.http.Metadata

trait S3Objects:
  def exists(bucket: String, objectKey: String): IO[Boolean]

object S3Objects:
  def impl(
      awsConfiguration: AwsConfiguration,
      httpClient: Client[IO],
  ): S3Objects = impl(
    awsConfiguration,
    request =>
      httpClient.expectOr[String](request)(response =>
        response
          .as[String]
          .flatMap(body =>
            IO.raiseError(AwsClientError(response.status.code, body.getBytes("UTF-8").nn)),
          ),
      ),
  )

  def impl(
      awsConfiguration: AwsConfiguration,
      requestHandler: Request[IO] => IO[String],
  ): S3Objects = (bucket: String, objectKey: String) =>
    exists(awsConfiguration, bucket, objectKey, requestHandler)

  private def exists(
      awsConfiguration: AwsConfiguration,
      bucket: String,
      objectKey: String,
      requestHandler: Request[IO] => IO[String],
  ): IO[Boolean] =
    val signer = S3Signer.impl(
      IO.pure(awsConfiguration.credentials),
      IO.pure(awsConfiguration.region.getOrElse(AwsRegion.EU_WEST_1)),
      IO.realTime.map(_.toSeconds).map(Timestamp(_, 0)),
    )
    for
      request <- signer.sign(bucket = bucket, metadata = listObjectsV2MetadataFrom(objectKey))
      bodyContent <- requestHandler(request)
      exists <- S3ResponseParser.keysFoundIn(bodyContent, objectKey).map(_ == 1L)
    yield exists

  private def listObjectsV2MetadataFrom(objectKey: String) = Metadata(query =
    Map("list-type" -> List("2"), "max-keys" -> List("1"), "prefix" -> List(objectKey)),
  )
