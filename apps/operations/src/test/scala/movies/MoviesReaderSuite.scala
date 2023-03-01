package es.eriktorr.lambda4s
package movies

import CollectionGenerators.nDistinct
import TemporalGenerators.{localDateTimeRangeGen, outOfLocalDateTimeRange, withinLocalDateTimeRange}
import database.mysql.MySqlTransactor
import infrastructure.DatabaseTestConfiguration.SakilaMySqlTest
import infrastructure.{DatabaseTestConfiguration, MySqlSuite, MySqlTestTransactor}
import movies.MoviesReader.CumulativeRevenue
import movies.MoviesReaderSuite.cumulativeRevenueTestCaseGen
import movies.writers.CustomerRowWriter.customerIdGen
import movies.writers.PaymentRowWriter
import movies.writers.PaymentRowWriter.{paymentIdGen, paymentRowGen, PaymentRow}
import movies.writers.RentalRowWriter.rentalIdGen
import movies.writers.StaffRowWriter.staffIdGen

import cats.Applicative
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
            _ <- for
              _ <- testTransactor.update("SET FOREIGN_KEY_CHECKS=0")
              _ <- PaymentRowWriter(testTransactor).add(testCase.paymentRows)
              _ <- testTransactor.update("SET FOREIGN_KEY_CHECKS=1")
            yield ()
            moviesReader = MoviesReader.impl(testTransactor.transactor)(using logger)
            obtained <- moviesReader.cumulativeRevenueDuring(testCase.dateTimeRange)
          yield obtained,
        )
        .assertEquals(testCase.expected, "Cumulative revenues are not the same")
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
    customerIds <- nDistinct(3, customerIdGen)
    paymentIds <- nDistinct(7, paymentIdGen)
    rentalIds <- nDistinct(7, rentalIdGen)
    staffIds <- nDistinct(3, staffIdGen)
    (selectedPaymentIds, paymentIdsOutOfDateTimeRange) = paymentIds.splitAt(3)
    selectedPayments <- selectedPaymentIds.traverse(paymentId =>
      paymentRowGen(
        customerIdGen = Gen.oneOf(customerIds),
        paymentDateGen = withinLocalDateTimeRange(dateTimeRange),
        paymentIdGen = paymentId,
        rentalIdGen = Gen.oneOf(rentalIds),
        staffIdGen = Gen.oneOf(staffIds),
      ),
    )
    paymentsOutOfDateTimeRange <- paymentIdsOutOfDateTimeRange.traverse(paymentId =>
      paymentRowGen(
        customerIdGen = Gen.oneOf(customerIds),
        paymentDateGen = outOfLocalDateTimeRange(dateTimeRange),
        paymentIdGen = paymentId,
        rentalIdGen = Gen.oneOf(rentalIds),
        staffIdGen = Gen.oneOf(staffIds),
      ),
    )
    expected = List.empty
  yield CumulativeRevenueTestCase(
    dateTimeRange,
    expected,
    selectedPayments ++ paymentsOutOfDateTimeRange,
  )
