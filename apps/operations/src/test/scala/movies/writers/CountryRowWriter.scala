package es.eriktorr.lambda4s
package movies.writers

import StringGenerators.stringBetween
import infrastructure.{MySqlTestTransactor, MySqlWriterSuite, RowWriter}
import movies.writers.CountryRowWriter.CountryRow

import cats.effect.IO
import org.scalacheck.Gen

import java.time.LocalDateTime

final class CountryRowWriter(testTransactor: MySqlTestTransactor)
    extends MySqlWriterSuite[CountryRow](testTransactor)
    with RowWriter[CountryRow]:
  override def add(rows: List[CountryRow]): IO[Unit] = super.add(
    rows,
    row => s"""INSERT INTO country (
              | country_id,
              | country
              |) VALUES (
              | ${row.country_id},
              | '${row.country}'
              |)""".stripMargin,
  )

object CountryRowWriter:
  final case class CountryRow(
      country_id: Short,
      country: String,
      last_update: Option[LocalDateTime],
  )

  val countryIdGen: Gen[Short] = Gen.choose[Short](0, Short.MaxValue)

  def countryRowGen(countryIdGen: Gen[Short]): Gen[CountryRow] = for
    country_id <- countryIdGen
    country <- stringBetween(3, 50)
  yield CountryRow(country_id, country, None)
