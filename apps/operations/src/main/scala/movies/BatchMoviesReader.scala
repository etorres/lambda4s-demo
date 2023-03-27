package es.eriktorr.lambda4s
package movies

import movies.MoviesReader.CumulativeRevenue

import cats.effect.IO
import cats.syntax.parallel.catsSyntaxParallelFlatTraverse1

import java.time.LocalDateTime

final class BatchMoviesReader(moviesReader: MoviesReader):
  /** Calculate the cumulative revenue of all stores during the given time period.
    *
    * @param dateRange
    *   filter the results within this period
    * @return
    *   cumulative revenue
    */
  def batchCumulativeRevenueDuring(
      dateTimeRange: DateRange[LocalDateTime],
  ): IO[List[CumulativeRevenue]] =
    dateTimeRange
      .weeksCovering(exactMatch = true)
      .parFlatTraverse(moviesReader.cumulativeRevenueDuring)
