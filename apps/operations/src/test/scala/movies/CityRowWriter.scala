package es.eriktorr.lambda4s
package movies

import StringGenerators.stringBetween
import infrastructure.{MySqlTestTransactor, MySqlWriterSuite}
import movies.CityRowWriter.CityRow

import cats.effect.IO
import org.scalacheck.Gen

import java.time.LocalDateTime

final class CityRowWriter(testTransactor: MySqlTestTransactor)
    extends MySqlWriterSuite[CityRow](testTransactor):
  def add(rows: List[CityRow]): IO[Unit] = super.add(
    rows,
    row => s"""INSERT INTO city (
              | city_id,
              | city,
              | country_id
              |) VALUES (
              | ${row.city_id},
              | '${row.city}',
              | ${row.country_id}
              |)""".stripMargin,
  )

object CityRowWriter:
  final case class CityRow(
      city_id: Short,
      city: String,
      country_id: Short,
      last_update: Option[LocalDateTime],
  )

  val cityIdGen: Gen[Short] = Gen.choose[Short](0, Short.MaxValue)

  def cityRowGen(cityIdGen: Gen[Short], countryIdGen: Gen[Short]): Gen[CityRow] = for
    city_id <- cityIdGen
    city <- stringBetween(3, 50)
    country_id <- countryIdGen
  yield CityRow(city_id, city, country_id, None)
