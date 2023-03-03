package es.eriktorr.lambda4s
package movies

import CollectionGenerators.nDistinct
import TemporalGenerators.{localDateTimeRangeGen, outOfLocalDateTimeRange, withinLocalDateTimeRange}
import database.Transactor
import database.mysql.MySqlTransactor
import infrastructure.DatabaseTestConfiguration.SakilaMySqlTest
import infrastructure.{DatabaseTestConfiguration, MySqlSuite, MySqlTestTransactor, RowWriter}
import movies.MoviesReader.{CumulativeRevenue, RatingCounter}
import movies.MoviesReaderSuite.{cumulativeRevenueTestCaseGen, filmsByRatingTestCaseGen, TestCase}
import movies.writers.CustomerRowWriter.customerIdGen
import movies.writers.FilmRowWriter.{filmIdGen, filmRowGen, FilmRow}
import movies.writers.LanguageRowWriter.languageIdGen
import movies.writers.PaymentRowWriter.{paymentIdGen, paymentRowGen, PaymentRow}
import movies.writers.RentalRowWriter.rentalIdGen
import movies.writers.StaffRowWriter.staffIdGen
import movies.writers.{FilmRowWriter, PaymentRowWriter}

import cats.Applicative
import cats.data.NonEmptyList
import cats.effect.IO
import cats.implicits.toTraverseOps
import org.scalacheck.Gen
import org.scalacheck.cats.implicits.*
import org.scalacheck.effect.PropF.forAllF
import org.typelevel.log4cats.Logger

import java.time.{LocalDate, LocalDateTime}
import scala.annotation.tailrec

final class MoviesReaderSuite extends MySqlSuite:
  test("should count the number of films grouped by their rating") {
    forAllF(filmsByRatingTestCaseGen) { testCase =>
      testWith(
        "Films grouped by their rating are not the same",
        testTransactor => FilmRowWriter(testTransactor),
        moviesReader => moviesReader.filmsByRating,
        testCase,
        Some(Ordering.by[RatingCounter, String](_.rating.name)),
      )
    }
  }

  test("should calculate the cumulative revenue during the given period") {
    forAllF(cumulativeRevenueTestCaseGen) { testCase =>
      testWith(
        "Cumulative revenues are not the same",
        testTransactor => PaymentRowWriter(testTransactor),
        moviesReader => moviesReader.cumulativeRevenueDuring(testCase.dateTimeRange),
        testCase,
      )
    }
  }

  private def testWith[A, R](
      clue: String,
      rowWriter: MySqlTestTransactor => RowWriter[R],
      run: MoviesReader => IO[List[A]],
      testCase: TestCase[A, R],
      sortWith: Option[Ordering[A]] = None,
  ) = MySqlTestTransactor
    .of(SakilaMySqlTest, munitExecutionContext)
    .use(testTransactor =>
      for
        logger <- Log4sFactory.impl().create
        _ <- for
          _ <- testTransactor.update("SET FOREIGN_KEY_CHECKS=0")
          _ <- rowWriter(testTransactor).add(testCase.rows.toList)
          _ <- testTransactor.update("SET FOREIGN_KEY_CHECKS=1")
        yield ()
        moviesReader = MoviesReader.impl(testTransactor.transactor)(using logger)
        obtained <- run(moviesReader)
      yield sortWith.fold(obtained)(ordering => obtained.sorted(using ordering)),
    )
    .assertEquals(
      sortWith.fold(testCase.expected)(ordering => testCase.expected.sorted(using ordering)),
      clue,
    )

object MoviesReaderSuite:
  sealed private trait TestCase[A, R]:
    val expected: List[A]
    val rows: NonEmptyList[R]

  final private case class FilmsByRatingTestCase(
      expected: List[RatingCounter],
      filmRows: List[FilmRow],
  ) extends TestCase[RatingCounter, FilmRow]:
    override val rows: NonEmptyList[FilmRow] = NonEmptyList.fromListUnsafe(filmRows)

  private val filmsByRatingTestCaseGen = for
    filmIds <- nDistinct(7, filmIdGen)
    films <- filmIds.traverse(filmId => filmRowGen(filmId, languageIdGen))
    expected = films
      .groupBy(_.rating)
      .map { case (rating, filmsInThisRating) =>
        RatingCounter(rating, filmsInThisRating.size)
      }
      .toList
  yield FilmsByRatingTestCase(expected, films)

  final private case class CumulativeRevenueTestCase(
      dateTimeRange: DateRange[LocalDateTime],
      expected: List[CumulativeRevenue],
      paymentRows: List[PaymentRow],
  ) extends TestCase[CumulativeRevenue, PaymentRow]:
    override val rows: NonEmptyList[PaymentRow] = NonEmptyList.fromListUnsafe(paymentRows)

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
    expected =
      @tailrec
      def cumulativeRevenue(
          payments: List[(LocalDate, Double)],
          accumulated: List[CumulativeRevenue],
      ): List[CumulativeRevenue] =
        payments match
          case Nil => accumulated
          case ::(head, next) =>
            val (paymentDate, amount) = (head._1, head._2)
            cumulativeRevenue(
              next,
              CumulativeRevenue(
                paymentDate,
                amount,
                BigDecimal(
                  accumulated.headOption
                    .map(_.cumulativeRevenue)
                    .getOrElse(0.0d) + amount,
                ).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble,
              ) :: accumulated,
            )

      val dailyPayments = selectedPayments
        .map(payment => payment.payment_date.toLocalDate.nn -> payment.amount)
        .groupBy { case (paymentDate, _) => paymentDate }
        .map { case (paymentDate, payments) =>
          paymentDate -> payments.map { case (_, amount) => amount }.sum
        }
        .toList
        .sortBy { case (paymentDate, _) => paymentDate }

      cumulativeRevenue(dailyPayments, List.empty).sortBy(_.paymentDate)
  yield CumulativeRevenueTestCase(
    dateTimeRange,
    expected,
    selectedPayments ++ paymentsOutOfDateTimeRange,
  )
