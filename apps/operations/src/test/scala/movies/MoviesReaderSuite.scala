package es.eriktorr.lambda4s
package movies

import CollectionGenerators.nDistinct
import TemporalGenerators.{localDateTimeRangeGen, outOfLocalDateTimeRange, withinLocalDateTimeRange}
import database.mysql.MySqlTransactor
import infrastructure.DatabaseTestConfiguration.SakilaMySqlTest
import infrastructure.{DatabaseTestConfiguration, MySqlSuite, MySqlTestTransactor}
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
      MySqlTestTransactor
        .of(SakilaMySqlTest, munitExecutionContext)
        .use(testTransactor =>
          for
            logger <- Log4sFactory.impl().create
            _ <- CountryRowWriter(testTransactor).add(testCase.countryRows)
            _ <- CityRowWriter(testTransactor).add(testCase.cityRows)
            _ <- PaymentRowWriter(testTransactor).add(testCase.paymentRows)
            moviesReader = MoviesReader.impl(testTransactor.transactor)(using logger)
            obtained <- moviesReader.cumulativeRevenueDuring(testCase.dateTimeRange)
          yield obtained,
        )
        .assertEquals(testCase.expected, "Cumulative revenues are not the same")
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
        customerIdGen = 1,
        paymentDateGen = withinLocalDateTimeRange(dateTimeRange),
        paymentIdGen = paymentId,
        rentalIdGen = 1,
        staffIdGen = 1,
      ),
    )
    paymentsOutOfDateTimeRange <- paymentIdsOutOfDateTimeRange.traverse(paymentId =>
      paymentRowGen(
        customerIdGen = 1,
        paymentDateGen = outOfLocalDateTimeRange(dateTimeRange),
        paymentIdGen = paymentId,
        rentalIdGen = 1,
        staffIdGen = 1,
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
