package smithy4s.aws

import cats.effect.IO
import org.http4s.client.dsl as http4sDsl
import org.http4s.{Header, Method, Request, Uri}
import org.typelevel.ci.CIStringSyntax
import smithy4s.aws.S3AccessStyle.{PathStyle, VirtualHostedStyle}
import smithy4s.http.Metadata
import smithy4s.http.internals.URIEncoderDecoder.encode as uriEncode

import java.net.URI

/** Amazon S3 AWS4 request signer.
  * @see
  *   [[https://docs.aws.amazon.com/AmazonS3/latest/API/sigv4-auth-using-authorization-header.html Authenticating Requests: Using the Authorization Header (AWS Signature Version 4)]]
  * @see
  *   [[https://docs.aws.amazon.com/AmazonS3/latest/API/sig-v4-header-based-auth.html Signature Calculations for the Authorization Header: Transferring Payload in a Single Chunk (AWS Signature Version 4)]]
  */
trait S3Signer:
  def sign(body: Option[String] = None, bucket: String, metadata: Metadata): IO[Request[IO]]

object S3Signer:
  import scala.language.unsafeNulls

  private val aws4Request = "aws4_request"
  private val signingAlgorithm = "AWS4-HMAC-SHA256"
  private val serviceName = "s3"

  def impl(
      awsCredentialsIO: IO[AwsCredentials],
      awsRegionIO: IO[AwsRegion],
      timestampIO: IO[Timestamp],
      s3AccessStyleIO: IO[S3AccessStyle],
      s3EndpointIO: IO[Option[URI]],
  ): S3Signer = (body: Option[String], bucket: String, metadata: Metadata) =>
    for
      awsCredentials <- awsCredentialsIO
      awsRegion <- awsRegionIO
      timestamp <- timestampIO
      s3AccessStyle <- s3AccessStyleIO
      s3Endpoint <- s3EndpointIO
      (host, headers) =
        import smithy4s.aws.kernel.AwsCrypto.*

        val credentialsScope =
          List(timestamp.conciseDate, awsRegion.value, serviceName, aws4Request).mkString("/")

        val host = hostFrom(awsRegion, bucket, s3AccessStyle, s3Endpoint)

        val httpMethod = Method.GET.name.toUpperCase

        val canonicalUri = "/"

        val canonicalQueryString =
          if metadata.query.isEmpty then ""
          else
            metadata.queryFlattened
              .sortBy(_._1)
              .toList
              .map { case (k, v) =>
                uriEncode(k) + "=" + uriEncode(v)
              }
              .mkString("&")

        val hashedPayload = sha256HexDigest(body.getOrElse(new String(Array.emptyByteArray)))

        val canonicalHeaders = List(
          s"host:$host",
          s"x-amz-content-sha256:$hashedPayload",
          s"x-amz-date:${timestamp.conciseDateTime}",
        ).mkString("\n") + "\n" // trailing line break

        val signedHeaders = "host;x-amz-content-sha256;x-amz-date"

        val canonicalRequest = List[String](
          httpMethod,
          canonicalUri,
          canonicalQueryString,
          canonicalHeaders,
          signedHeaders,
          hashedPayload,
        ).mkString("\n")

        val canonicalRequestHash = sha256HexDigest(canonicalRequest)

        val stringToSign = List[String](
          signingAlgorithm,
          timestamp.conciseDateTime,
          credentialsScope,
          canonicalRequestHash,
        ).mkString("\n")

        val signingKey =
          val kSecret = binaryFromString(s"AWS4${awsCredentials.secretAccessKey}")
          val kDate = hmacSha256(timestamp.conciseDate, kSecret)
          val kRegion = hmacSha256(awsRegion.value, kDate)
          val kService = hmacSha256(serviceName, kRegion)
          val kSigning = hmacSha256(aws4Request, kService)
          kSigning

        val signature = toHexString(hmacSha256(stringToSign, signingKey))

        val authorizationHeader = List(
          s"$signingAlgorithm Credential=${awsCredentials.accessKeyId}/$credentialsScope",
          s"SignedHeaders=$signedHeaders",
          s"Signature=$signature",
        ).mkString(", ")

        val headers = List(
          Header.Raw(ci"Authorization", authorizationHeader),
          Header.Raw(ci"host", host),
          Header.Raw(ci"x-amz-content-sha256", hashedPayload),
          Header.Raw(ci"x-amz-date", timestamp.conciseDateTime),
        )

        (host, headers)
      target <- IO.fromEither(
        Uri
          .fromString(uriFrom(bucket, host, s3AccessStyle, s3Endpoint))
          .map(_.withQueryParams(metadata.queryFlattened.sortBy(_._1).toMap)),
      )
      request =
        import http4sDsl.io.*
        Method.GET(target, headers)
    yield request

  private def hostFrom(
      awsRegion: AwsRegion,
      bucket: String,
      s3AccessStyle: S3AccessStyle,
      s3Endpoint: Option[URI],
  ) =
    val baseHost =
      s3Endpoint.map(_.getHost).getOrElse(s"$serviceName.${awsRegion.value}.amazonaws.com")
    s3AccessStyle match
      case PathStyle => baseHost
      case VirtualHostedStyle => s"$bucket.$baseHost"

  private def uriFrom(
      bucket: String,
      host: String,
      s3AccessStyle: S3AccessStyle,
      s3Endpoint: Option[URI],
  ) =
    val baseUri = s3Endpoint
      .map(uri => s"${uri.getScheme}://${uri.getHost}:${uri.getPort}")
      .getOrElse(s"https://$host")
    s3AccessStyle match
      case PathStyle => s"$baseUri/$bucket"
      case VirtualHostedStyle => baseUri
