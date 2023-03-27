package es.eriktorr.lambda4s
package movies

import infrastructure.FakeMoviesReader
import infrastructure.FakeMoviesReader.MoviesReaderState
import movies.BatchMoviesReaderSuite.cumulativeRevenueOrdering
import movies.MoviesReader.CumulativeRevenue

import cats.effect.{IO, Ref}
import munit.CatsEffectSuite

import java.time.{LocalDate, Month}

final class BatchMoviesReaderSuite extends CatsEffectSuite:
  test("should calculate the cumulative revenue during the given period using batch operations") {
    val dateRange = DateRange(
      LocalDate.of(2020, Month.DECEMBER, 15).nn.atStartOfDay().nn,
      LocalDate.of(2021, Month.JANUARY, 1).nn.atStartOfDay().nn,
    )

    val week1 = DateRange(
      LocalDate.of(2020, Month.DECEMBER, 15).nn.atStartOfDay().nn,
      LocalDate.of(2020, Month.DECEMBER, 19).nn.atStartOfDay().nn,
    )
    val week2 = DateRange(
      LocalDate.of(2020, Month.DECEMBER, 20).nn.atStartOfDay().nn,
      LocalDate.of(2020, Month.DECEMBER, 26).nn.atStartOfDay().nn,
    )
    val week3 = DateRange(
      LocalDate.of(2020, Month.DECEMBER, 27).nn.atStartOfDay().nn,
      LocalDate.of(2021, Month.JANUARY, 1).nn.atStartOfDay().nn,
    )

    val cumulativeRevenueWeek1 =
      CumulativeRevenue(LocalDate.of(2020, Month.DECEMBER, 15).nn, 12.0d, 12.0d)
    val cumulativeRevenueWeek2 =
      CumulativeRevenue(LocalDate.of(2020, Month.DECEMBER, 23).nn, 13.0d, 25.0d)
    val cumulativeRevenueWeek3 =
      CumulativeRevenue(LocalDate.of(2020, Month.DECEMBER, 29).nn, 4.0d, 29.0d)

    val cumulativeRevenues = Map(
      week1 -> List(cumulativeRevenueWeek1),
      week2 -> List(cumulativeRevenueWeek2),
      week3 -> List(cumulativeRevenueWeek3),
    )

    (for
      moviesReaderStateRef <- Ref.of[IO, MoviesReaderState](
        MoviesReaderState.empty.setCumulativeRevenues(cumulativeRevenues),
      )
      moviesReader = FakeMoviesReader(moviesReaderStateRef)
      batchMoviesReader = BatchMoviesReader(moviesReader)
      obtained <- batchMoviesReader.batchCumulativeRevenueDuring(dateRange)
    yield obtained.sorted)
      .assertEquals(
        List(cumulativeRevenueWeek1, cumulativeRevenueWeek2, cumulativeRevenueWeek3).sorted,
      )
  }

object BatchMoviesReaderSuite:
  given cumulativeRevenueOrdering: Ordering[CumulativeRevenue] =
    Ordering.by[CumulativeRevenue, LocalDate](_.paymentDate)
