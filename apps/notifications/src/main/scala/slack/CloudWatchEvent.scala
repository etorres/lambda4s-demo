package es.eriktorr.lambda4s
package slack

import slack.CloudWatchEvent.CloudWatchLogs

import io.circe.Decoder

/** Amazon CloudWatch Logs events.
  *
  * @see
  *   [[https://docs.aws.amazon.com/lambda/latest/dg/lambda-services.html Using AWS Lambda with other services]]
  * @see
  *   [[https://docs.aws.amazon.com/lambda/latest/dg/services-cloudwatchlogs.html Using Lambda with CloudWatch Logs]]
  */
final case class CloudWatchEvent(awslogs: CloudWatchLogs)

object CloudWatchEvent:
  final case class CloudWatchLogs(data: String)

  object CloudWatchLogs:
    given cloudWatchLogsJsonDecoder: Decoder[CloudWatchLogs] =
      Decoder.forProduct1("data")(CloudWatchLogs.apply)

    given cloudWatchEventJsonDecoder: Decoder[CloudWatchEvent] =
      Decoder.forProduct1("awslogs")(CloudWatchEvent.apply)
