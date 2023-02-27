package es.eriktorr.lambda4s
package slack

import slack.SlackMessage.SlackAttachment

import io.circe.Encoder

final case class SlackMessage(text: String, attachments: List[SlackAttachment])

object SlackMessage:
  final case class SlackAttachment(text: String)

  object SlackAttachment:
    given slackAttachmentJsonEncoder: Encoder[SlackAttachment] =
      Encoder.forProduct1("text")(_.text)

    given slackMessageJsonEncoder: Encoder[SlackMessage] =
      Encoder.forProduct2("text", "attachments")(x => (x.text, x.attachments))
