package es.eriktorr.lambda4s
package movies

import CollectionGenerators.nDistinct
import TemporalGenerators.{localDateTimeRangeGen, outOfLocalDateTimeRange, withinLocalDateTimeRange}
import database.mysql.MySqlTransactor
import infrastructure.DatabaseTestConfiguration.SakilaMySqlTest
import infrastructure.{DatabaseTestConfiguration, MySqlSuite}
import movies.MoviesReader.CumulativeRevenue
import movies.MoviesReaderSuite.cumulativeRevenueTestCaseGen
import movies.PaymentRowWriter.{paymentIdGen, paymentRowGen, PaymentRow}

import cats.implicits.toTraverseOps
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
        paymentRowWriter = PaymentRowWriter(SakilaMySqlTest, munitExecutionContext)
        _ <- paymentRowWriter.add(testCase.paymentRows)
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
      dateTimeRange: DateRange[LocalDateTime],
      expected: List[CumulativeRevenue],
      paymentRows: List[PaymentRow],
  )

  private val cumulativeRevenueTestCaseGen = for
    dateTimeRange <- localDateTimeRangeGen
    paymentIds <- nDistinct(7, paymentIdGen)
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
    dateTimeRange,
    expected,
    selectedPayments ++ paymentsOutOfDateTimeRange,
  )
