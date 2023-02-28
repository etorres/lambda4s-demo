package es.eriktorr.lambda4s
package movies

import StringGenerators.stringOfLength
import infrastructure.{MySqlTestTransactor, MySqlWriterSuite}
import movies.LanguageRowWriter.LanguageRow

import cats.effect.IO
import org.scalacheck.Gen

import java.time.LocalDateTime

final class LanguageRowWriter(testTransactor: MySqlTestTransactor)
    extends MySqlWriterSuite[LanguageRow](testTransactor):
  def add(rows: List[LanguageRow]): IO[Unit] = super.add(
    rows,
    row => s"""INSERT INTO language (
              | language_id,
              | name
              |) VALUES (
              | ${row.language_id},
              | '${row.name}'
              |)""".stripMargin,
  )

object LanguageRowWriter:
  final case class LanguageRow(language_id: Byte, name: String, last_update: Option[LocalDateTime])

  val languageIdGen: Gen[Byte] = Gen.choose[Byte](0, Byte.MaxValue)

  def languageRowGen(languageIdGen: Gen[Byte]): Gen[LanguageRow] = for
    language_id <- languageIdGen
    name <- stringOfLength(20)
  yield LanguageRow(language_id, name, None)
