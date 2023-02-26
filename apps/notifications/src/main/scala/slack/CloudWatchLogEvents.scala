package es.eriktorr.lambda4s
package slack

import slack.CloudWatchLogEvents.CloudWatchLogEvent

import io.circe.Decoder

final case class CloudWatchLogEvents(
    logEvents: List[CloudWatchLogEvent],
    logGroup: String,
    logStream: String,
    messageType: String,
    owner: String,
    subscriptionFilters: List[String],
)

object CloudWatchLogEvents:
  final case class CloudWatchLogEvent(id: String, message: String, timestamp: Long)

  implicit val cloudWatchLogEventJsonDecoder: Decoder[CloudWatchLogEvent] =
    Decoder.forProduct3("id", "message", "timestamp")(CloudWatchLogEvent.apply)

  implicit val cloudWatchLogEventsJsonDecoder: Decoder[CloudWatchLogEvents] =
    Decoder.forProduct6(
      "logEvents",
      "logGroup",
      "logStream",
      "messageType",
      "owner",
      "subscriptionFilters",
    )(
      CloudWatchLogEvents.apply,
    )
