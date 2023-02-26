package es.eriktorr.lambda4s
package slack

import slack.SlackMessage.SlackAttachment.slackMessageJsonEncoder

import cats.effect.IO
import io.circe.syntax.EncoderOps
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.client.{dsl as http4sDsl, Client}
import org.http4s.{Method, Uri}

import scala.util.control.NoStackTrace

trait SlackWebhook:
  def post(message: SlackMessage): IO[Unit]

object SlackWebhook:
  def impl(httpClient: Client[IO], slackConfiguration: SlackConfiguration): SlackWebhook =
    (message: SlackMessage) =>
      import http4sDsl.io.*
      for
        target <- IO.fromEither(Uri.fromString(slackConfiguration.webhookEndpoint.toString))
        _ <- httpClient.expectOr[String](Method.POST(message.asJson, target))(response =>
          response
            .as[String]
            .flatMap(body => IO.raiseError(SlackError(response.status.code, body))),
        )
      yield ()

  final private case class SlackError(statusCode: Int, body: String) extends NoStackTrace:
    override def getMessage: String = s"Request failed with status $statusCode and body $body"
