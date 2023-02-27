package es.eriktorr.lambda4s

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.Temporal

final case class DateRange[T <: Temporal](from: T, to: T)

object DateRange:
  trait DateRangeFormatter[T <: Temporal, F]:
    def format(dateRange: DateRange[T], formatter: F): (String, String)

  given DateRangeFormatter[LocalDate, DateTimeFormatter] with
    override def format(
        dateRange: DateRange[LocalDate],
        formatter: DateTimeFormatter,
    ): (String, String) = (dateRange.from.format(formatter).nn, dateRange.to.format(formatter).nn)

  extension [T <: Temporal, F](self: DateRange[T])
    def format(formatter: F)(using dateRangeFormatter: DateRangeFormatter[T, F]): (String, String) =
      dateRangeFormatter.format(self, formatter)
