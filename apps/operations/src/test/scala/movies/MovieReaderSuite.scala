package es.eriktorr.lambda4s
package movies

import TemporalGenerators.localDateRangeGen
import infrastructure.MySqlSuite
import movies.MovieReader.CumulativeRevenue
import movies.MovieReaderSuite.cumulativeRevenueTestCaseGen

import org.scalacheck.effect.PropF.forAllF

import java.time.LocalDate

final class MovieReaderSuite extends MySqlSuite:

  test("should count the number of films grouped by their rating") {
    fail("not implemented")
  }

  test("should calculate the cumulative revenue during the given period") {
    forAllF(cumulativeRevenueTestCaseGen) { testCase =>
      for
        logger <- Log4sFactory.impl().create
        logger <- Log4sFactory.impl().create
      yield ()
    }
  }

object MovieReaderSuite:
  final private case class CumulativeRevenueTestCase(
      dateRange: DateRange[LocalDate],
      expected: List[CumulativeRevenue],
  )

  private val cumulativeRevenueTestCaseGen = for
    dateRange <- localDateRangeGen
    expected = List.empty
  yield CumulativeRevenueTestCase(dateRange, expected)
