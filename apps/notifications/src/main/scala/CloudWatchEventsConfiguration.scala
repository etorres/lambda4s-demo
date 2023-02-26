package es.eriktorr.lambda4s

import slack.SlackConfiguration

import cats.effect.IO
import cats.implicits.catsSyntaxEither
import ciris.{env, ConfigDecoder, ConfigError}

import java.net.URI
import scala.util.Try

final case class CloudWatchEventsConfiguration(slackConfiguration: SlackConfiguration)

object CloudWatchEventsConfiguration:
  def load: IO[CloudWatchEventsConfiguration] =
    implicit val uriConfigDecoder: ConfigDecoder[String, URI] =
      ConfigDecoder[String, String].mapEither { case (_, uri) =>
        Try(URI.create(uri).nn).toEither.leftMap(error => ConfigError(error.getMessage.nn))
      }

    val webhookEndpointConfig =
      env("NOTIFICATIONS_SLACK_WEBHOOK_ENDPOINT").as[URI].map(SlackConfiguration.apply)

    val config = webhookEndpointConfig.map(CloudWatchEventsConfiguration.apply)

    config.load[IO]
