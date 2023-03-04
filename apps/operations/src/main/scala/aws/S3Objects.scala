package es.eriktorr.lambda4s
package aws

import cats.effect.IO
import org.http4s.client.Client
import smithy4s.aws.*
import smithy4s.http.Metadata

trait S3Objects:
  def exists(bucket: String, objectKey: String): IO[Boolean]

object S3Objects:
  def impl(
      awsCredentials: AwsCredentials,
      awsRegion: AwsRegion,
      httpClient: Client[IO],
  ): S3Objects = new S3Objects:
    override def exists(bucket: String, objectKey: String): IO[Boolean] =
      val signer = S3Signer.impl(
        IO.pure(awsCredentials),
        IO.pure(awsRegion),
        IO.realTime.map(_.toSeconds).map(Timestamp(_, 0)),
      )
      for
        request <- signer.sign(bucket = bucket, metadata = listObjectsV2MetadataFrom(objectKey))
        bodyContent <- httpClient.expectOr[String](request)(response =>
          response
            .as[String]
            .flatMap(body =>
              IO.raiseError(AwsClientError(response.status.code, body.getBytes("UTF-8").nn)),
            ),
        )
        existsInDataLake =
          val keyCountPattern = raw"<KeyCount>1</KeyCount>".r.unanchored
          val keyPattern = raw"<Key>(?<key>[a-zA-Z0-9\-_=/]+)</Key>".r.unanchored
          val oneLineXml = bodyContent.replaceAll("\\R", "").nn
          keyCountPattern.matches(oneLineXml) && (oneLineXml match
            case keyPattern(key) => key == objectKey
            case _ => false
          )
      yield existsInDataLake

    private def listObjectsV2MetadataFrom(objectKey: String) =
      Metadata(
        query = Map("list-type" -> List("2"), "max-keys" -> List("1"), "prefix" -> List(objectKey)),
      )
