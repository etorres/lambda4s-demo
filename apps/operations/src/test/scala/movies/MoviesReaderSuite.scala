package es.eriktorr.lambda4s
package movies

import CollectionGenerators.nDistinct
import TemporalGenerators.{localDateTimeRangeGen, outOfLocalDateTimeRange, withinLocalDateTimeRange}
import database.mysql.MySqlTransactor
import infrastructure.DatabaseTestConfiguration.SakilaMySqlTest
import infrastructure.{DatabaseTestConfiguration, MySqlSuite}
import movies.CityRowWriter.{cityIdGen, cityRowGen, CityRow}
import movies.CountryRowWriter.{countryIdGen, countryRowGen, CountryRow}
import movies.MoviesReader.CumulativeRevenue
import movies.MoviesReaderSuite.cumulativeRevenueTestCaseGen
import movies.PaymentRowWriter.{paymentIdGen, paymentRowGen, PaymentRow}

import cats.implicits.toTraverseOps
import org.scalacheck.Gen
import org.scalacheck.cats.implicits.*
import org.scalacheck.effect.PropF.forAllF

import java.time.{LocalDate, LocalDateTime}

final class MoviesReaderSuite extends MySqlSuite:

  test("should count the number of films grouped by their rating".ignore) {
    fail("not implemented")
  }

  test("should calculate the cumulative revenue during the given period") {
    forAllF(cumulativeRevenueTestCaseGen) { testCase =>
      for
        logger <- Log4sFactory.impl().create
        _ <- CountryRowWriter(SakilaMySqlTest, munitExecutionContext).add(testCase.countryRows)
        _ <- CityRowWriter(SakilaMySqlTest, munitExecutionContext).add(testCase.cityRows)
//        _ <- PaymentRowWriter(SakilaMySqlTest, munitExecutionContext).add(testCase.paymentRows)
        obtained <- MySqlTransactor.of(SakilaMySqlTest.config, munitExecutionContext).use {
          transactor =>
            val moviesReader = MoviesReader.impl(transactor)(using logger)
            moviesReader.cumulativeRevenueDuring(testCase.dateTimeRange)
        }
      yield assertEquals(obtained, testCase.expected, "Cumulative revenues are not the same")
    }
  }

object MoviesReaderSuite:
  final private case class CumulativeRevenueTestCase(
      cityRows: List[CityRow],
      countryRows: List[CountryRow],
      dateTimeRange: DateRange[LocalDateTime],
      expected: List[CumulativeRevenue],
      paymentRows: List[PaymentRow],
  )

  private val cumulativeRevenueTestCaseGen = for
    countryIds <- nDistinct(3, countryIdGen)
    cityIds <- nDistinct(3, cityIdGen)
    dateTimeRange <- localDateTimeRangeGen
    paymentIds <- nDistinct(7, paymentIdGen)
    countries <- countryIds.traverse(countryRowGen(_))
    cities <- cityIds.traverse(cityRowGen(_, Gen.oneOf(countryIds)))
    (selectedPaymentIds, paymentIdsOutOfDateTimeRange) = paymentIds.splitAt(3)
    selectedPayments <- selectedPaymentIds.traverse(paymentId =>
      paymentRowGen(
        paymentIdGen = paymentId,
        paymentDateGen = withinLocalDateTimeRange(dateTimeRange),
      ),
    )
    paymentsOutOfDateTimeRange <- paymentIdsOutOfDateTimeRange.traverse(paymentId =>
      paymentRowGen(
        paymentIdGen = paymentId,
        paymentDateGen = outOfLocalDateTimeRange(dateTimeRange),
      ),
    )
    expected = List.empty
  yield CumulativeRevenueTestCase(
    cities,
    countries,
    dateTimeRange,
    expected,
    selectedPayments ++ paymentsOutOfDateTimeRange,
  )
