package es.eriktorr.lambda4s
package movies

import StringGenerators.stringBetween
import TemporalGenerators.localDateTimeGen
import infrastructure.DatabaseTestConfiguration.mysqlDateTimeFormatter
import infrastructure.{DatabaseTestConfiguration, MySqlWriterSuite}
import movies.CountryRowWriter.CountryRow

import cats.effect.IO
import com.fortysevendeg.scalacheck.datetime.jdk8.ArbitraryJdk8.arbLocalDateTimeJdk8
import org.scalacheck.Gen

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext

final class CountryRowWriter(
    databaseTestConfiguration: DatabaseTestConfiguration,
    executionContext: ExecutionContext,
) extends MySqlWriterSuite[CountryRow](databaseTestConfiguration, executionContext):
  def add(rows: List[CountryRow]): IO[Unit] = super.add(
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

  def countryRowGen(countryIdGen: Gen[Short] = countryIdGen): Gen[CountryRow] = for
    country_id <- countryIdGen
    country <- stringBetween(3, 50)
  yield CountryRow(country_id, country, None)
