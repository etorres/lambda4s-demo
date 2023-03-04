package es.eriktorr.lambda4s
package aws

import cats.effect.IO
import org.http4s.Request
import org.http4s.client.Client
import smithy4s.aws.*
import smithy4s.http.Metadata
import sttp.capabilities.WebSockets
import sttp.client3.SttpBackend

trait S3Objects:
  def exists(bucket: String, objectKey: String): IO[Boolean]

object S3Objects:
  def impl(
      awsCredentials: AwsCredentials,
      awsRegion: AwsRegion,
      backend: SttpBackend[IO, WebSockets],
  ): S3Objects = (bucket: String, objectKey: String) =>
    import sttp.client3.{basicRequest, UriContext}
    exists(
      awsCredentials,
      awsRegion,
      bucket,
      objectKey,
      request =>
        for
          response <- basicRequest
            .get(uri"${request.uri}")
            .headers(request.headers.headers.map(x => x.name.toString -> x.value).toMap)
            .send(backend)
          body <- response.body
            .fold(
              error =>
                IO.raiseError(AwsClientError(response.code.code, error.getBytes("UTF-8").nn)),
              IO.pure,
            )
        yield body,
    )

  def impl(
      awsCredentials: AwsCredentials,
      awsRegion: AwsRegion,
      httpClient: Client[IO],
  ): S3Objects = (bucket: String, objectKey: String) =>
    exists(
      awsCredentials,
      awsRegion,
      bucket,
      objectKey,
      request =>
        httpClient.expectOr[String](request)(response =>
          response
            .as[String]
            .flatMap(body =>
              IO.raiseError(AwsClientError(response.status.code, body.getBytes("UTF-8").nn)),
            ),
        ),
    )

  private def exists(
      awsCredentials: AwsCredentials,
      awsRegion: AwsRegion,
      bucket: String,
      objectKey: String,
      requestHandler: Request[IO] => IO[String],
  ): IO[Boolean] =
    val signer = S3Signer.impl(
      IO.pure(awsCredentials),
      IO.pure(awsRegion),
      IO.realTime.map(_.toSeconds).map(Timestamp(_, 0)),
    )
    for
      request <- signer.sign(bucket = bucket, metadata = listObjectsV2MetadataFrom(objectKey))
      bodyContent <- requestHandler(request)
      exists =
        val keyCountPattern = raw"<KeyCount>1</KeyCount>".r.unanchored
        val keyPattern = raw"<Key>(?<key>[a-zA-Z0-9\-_=/]+)</Key>".r.unanchored
        val oneLineXml = bodyContent.replaceAll("\\R", "").nn
        keyCountPattern.matches(oneLineXml) && (oneLineXml match
          case keyPattern(key) => key == objectKey
          case _ => false
        )
    yield exists

  private def listObjectsV2MetadataFrom(objectKey: String) = Metadata(query =
    Map("list-type" -> List("2"), "max-keys" -> List("1"), "prefix" -> List(objectKey)),
  )
