package es.eriktorr.lambda4s

import slack.CloudWatchEvent.CloudWatchLogs.cloudWatchEventJsonDecoder
import slack.SlackMessage.SlackAttachment
import slack.{CloudWatchEvent, CloudWatchLogEvents, SlackMessage, SlackWebhook}

import cats.effect.{IO, Resource}
import feral.lambda.{Context, INothing, IOLambda}
import fs2.compression.Compression
import fs2.io.compression.fs2ioCompressionForAsync
import io.circe.parser.decode
import org.http4s.ember.client.EmberClientBuilder

object CloudWatchEventsHandler extends IOLambda.Simple[CloudWatchEvent, INothing]:
  override def apply(
      event: CloudWatchEvent,
      context: Context[IO],
      init: Init,
  ): IO[Option[INothing]] = (for
    configuration <- Resource.eval(CloudWatchEventsConfiguration.load)
    httpClient <- EmberClientBuilder.default[IO].build
  yield (configuration, httpClient)).use { case (configuration, httpClient) =>
    for
      cloudWatchLogEvents <- decodePayload(event)
      _ <-
        val slackMessage = SlackMessage(
          text = cloudWatchLogEvents.logGroup,
          attachments =
            cloudWatchLogEvents.logEvents.map(logEvent => SlackAttachment(logEvent.message)),
        )
        val slackWebhook = SlackWebhook.impl(httpClient, configuration.slackConfiguration)
        slackWebhook.post(slackMessage)
    yield None
  }

  private def decodePayload(event: CloudWatchEvent) =
    val compressedPayload = fs2.Stream.emit(event.awslogs.data).through(fs2.text.base64.decode[IO])
    val uncompressedPayload = compressedPayload
      .through(Compression[IO].gunzip())
      .flatMap(_.content)
      .through(fs2.text.utf8.decode)
    uncompressedPayload.compile.string.flatMap(str =>
      IO.fromEither(decode[CloudWatchLogEvents](str)),
    )
