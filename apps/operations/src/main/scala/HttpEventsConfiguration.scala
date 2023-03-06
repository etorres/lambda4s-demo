package es.eriktorr.lambda4s

import database.DatabaseConfiguration

import cats.effect.IO
import cats.implicits.catsSyntaxTuple3Parallel
import ciris.{default, env, ConfigValue, Effect}
import smithy4s.aws.{AwsConfiguration, AwsCredentials}

final case class HttpEventsConfiguration(
    awsConfiguration: AwsConfiguration,
    databaseConfiguration: DatabaseConfiguration,
    s3Bucket: String,
)

object HttpEventsConfiguration:
  def load: IO[HttpEventsConfiguration] =
    val awsCredentialsConfig: ConfigValue[Effect, AwsCredentials] = (
      env("LAMBDA4S_AWS_ACCESS_KEY_ID").as[String],
      env("LAMBDA4S_AWS_SECRET_ACCESS_KEY").as[String],
      default(Option.empty[String]).as[Option[String]],
    ).parMapN(AwsCredentials.Default.apply)

    ???
