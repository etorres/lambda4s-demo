package es.eriktorr.lambda4s
package slack

import slack.SlackMessage.SlackAttachment

import io.circe.Encoder

final case class SlackMessage(text: String, attachments: List[SlackAttachment])

object SlackMessage:
  final case class SlackAttachment(text: String)

  object SlackAttachment:
    implicit val slackAttachmentJsonEncoder: Encoder[SlackAttachment] =
      Encoder.forProduct1("text")(_.text)

    implicit val slackMessageJsonEncoder: Encoder[SlackMessage] =
      Encoder.forProduct2("text", "attachments")(x => (x.text, x.attachments))
