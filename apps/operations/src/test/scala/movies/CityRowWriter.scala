package es.eriktorr.lambda4s
package movies

import StringGenerators.stringBetween
import TemporalGenerators.localDateTimeGen
import infrastructure.DatabaseTestConfiguration.mysqlDateTimeFormatter
import infrastructure.{DatabaseTestConfiguration, MySqlWriterSuite}
import movies.CityRowWriter.CityRow
import movies.CountryRowWriter.countryIdGen

import cats.effect.IO
import com.fortysevendeg.scalacheck.datetime.jdk8.ArbitraryJdk8.arbLocalDateTimeJdk8
import org.scalacheck.Gen

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext

final class CityRowWriter(
    databaseTestConfiguration: DatabaseTestConfiguration,
    executionContext: ExecutionContext,
) extends MySqlWriterSuite[CityRow](databaseTestConfiguration, executionContext):
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

  def cityRowGen(
      cityIdGen: Gen[Short] = cityIdGen,
      countryIdGen: Gen[Short] = countryIdGen,
  ): Gen[CityRow] = for
    city_id <- cityIdGen
    city <- stringBetween(3, 50)
    country_id <- countryIdGen
  yield CityRow(city_id, city, country_id, None)
