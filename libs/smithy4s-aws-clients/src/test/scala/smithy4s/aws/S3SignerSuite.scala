package smithy4s.aws

import cats.effect.IO
import munit.CatsEffectSuite
import org.typelevel.ci.CIStringSyntax
import smithy4s.http.Metadata

final class S3SignerSuite extends CatsEffectSuite:
  test("should sign a request") {
    val awsConfiguration = AwsTestConfiguration.LocalStack.awsConfiguration

    IO.realTime.map(_.toSeconds).map(Timestamp(_, 0)).flatMap { timestamp =>
      val signer = S3Signer.impl(
        IO.pure(awsConfiguration.credentials),
        IO.pure(awsConfiguration.region.get),
        IO.pure(timestamp),
      )
      (for
        signedRequest <- signer
          .sign(
            bucket = "test_bucket",
            metadata = Metadata(
              query = Map(
                "list-type" -> List("2"),
                "max-keys" -> List("1"),
                "prefix" -> List("test_object_key"),
              ),
            ),
          )
        authorizationHeaders = signedRequest.headers.get(ci"Authorization")
        awsSignature = authorizationHeaders.map(_.head.value)
        signaturePrefix = awsSignature.map(_.substring(0, 147)).getOrElse("")
        signatureLength = awsSignature.map(_.substring(147).nn.length).getOrElse(0)
      yield (signaturePrefix, signatureLength)).assertEquals(
        s"AWS4-HMAC-SHA256 Credential=AKIAIOSFODNN7EXAMPLE/${timestamp.conciseDate}/eu-west-1/s3/aws4_request, SignedHeaders=host;x-amz-content-sha256;x-amz-date, Signature=" -> 64,
      )
    }
  }
