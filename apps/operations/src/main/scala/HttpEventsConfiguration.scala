package es.eriktorr.lambda4s

import database.DatabaseConfiguration

import cats.effect.IO
import cats.syntax.parallel.*
import ciris.*
import com.comcast.ip4s.{host, port, Host, Port}
import smithy4s.aws.{AwsConfiguration, AwsCredentials, AwsRegion}

final case class HttpEventsConfiguration(
    awsConfiguration: AwsConfiguration,
    databaseConfiguration: DatabaseConfiguration,
    s3Bucket: String,
)

object HttpEventsConfiguration:
  def load: IO[HttpEventsConfiguration] =
    given ConfigDecoder[String, AwsRegion] = ConfigDecoder.lift(region => Right(AwsRegion(region)))

    given ConfigDecoder[String, Host] = ConfigDecoder.lift(host =>
      Host.fromString(host) match
        case Some(value) => Right(value)
        case None => Left(ConfigError("Invalid host")),
    )

    given ConfigDecoder[String, Port] = ConfigDecoder.lift(port =>
      Port.fromString(port) match
        case Some(value) => Right(value)
        case None => Left(ConfigError("Invalid port")),
    )

    val awsCredentials: ConfigValue[Effect, AwsCredentials] = (
      env("LAMBDA4S_AWS_ACCESS_KEY_ID").as[String],
      env("LAMBDA4S_AWS_SECRET_ACCESS_KEY").as[String],
      default(Option.empty[String]).as[Option[String]],
    ).parMapN(AwsCredentials.Default.apply)

    val awsConfiguration =
      (awsCredentials, env("LAMBDA4S_AWS_REGION").as[AwsRegion].option)
        .parMapN(AwsConfiguration.apply)

    val databaseConfiguration = (
      env("LAMBDA4S_DATABASE_NAME").as[String].default("sakila"),
      env("LAMBDA4S_DATABASE_HOST").as[Host].default(host"localhost"),
      env("LAMBDA4S_DATABASE_PASSWORD").as[String].secret,
      env("LAMBDA4S_DATABASE_PORT").as[Port].default(port"3306"),
      env("LAMBDA4S_DATABASE_USER").as[String],
    ).parMapN(DatabaseConfiguration.apply)

    val s3BucketConfig = env("LAMBDA4S_S3_BUCKET").as[String]

    val config: ConfigValue[Effect, HttpEventsConfiguration] =
      (awsConfiguration, databaseConfiguration, s3BucketConfig).parMapN(
        HttpEventsConfiguration.apply,
      )

    config.load[IO]
