package es.eriktorr.lambda4s
package aws

import cats.effect.IO
import fs2.Stream
import fs2.data.xml.xpath.filter
import fs2.data.xml.xpath.literals.xpath
import fs2.data.xml.{collector, events}

object S3ResponseParser:
  def keysFoundIn(xml: String, key: String): IO[Long] = Stream
    .emit(xml)
    .through(events[IO, String]())
    .through(filter.collect(xpath"/*/Contents/Key", collector.raw(false), deterministic = false))
    .filter(_ == s"<Key>$key</Key>")
    .compile
    .count
