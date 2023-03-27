package es.eriktorr.lambda4s
package infrastructure

import infrastructure.FakeMoviesReader.MoviesReaderState
import movies.MoviesReader
import movies.MoviesReader.{CumulativeRevenue, RatingCounter}

import cats.effect.{IO, Ref}

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

final class FakeMoviesReader(stateRef: Ref[IO, MoviesReaderState]) extends MoviesReader:
  override def filmsByRating: IO[List[RatingCounter]] =
    IO.raiseError(IllegalStateException("Not implemented"))

  override def cumulativeRevenueDuring(
      dateTimeRange: DateRange[LocalDateTime],
  ): IO[List[CumulativeRevenue]] =
    stateRef.get.map(_.cumulativeRevenues.getOrElse(dateTimeRange, List.empty))

object FakeMoviesReader:
  final case class MoviesReaderState(
      cumulativeRevenues: Map[DateRange[LocalDateTime], List[CumulativeRevenue]],
  ):
    def setCumulativeRevenues(
        newCumulativeRevenues: Map[DateRange[LocalDateTime], List[CumulativeRevenue]],
    ): MoviesReaderState = copy(newCumulativeRevenues)

  object MoviesReaderState:
    def empty: MoviesReaderState = MoviesReaderState(Map.empty)
