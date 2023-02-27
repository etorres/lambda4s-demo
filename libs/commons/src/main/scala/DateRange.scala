package es.eriktorr.lambda4s

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.Temporal

final case class DateRange[T <: Temporal](from: T, to: T)

object DateRange:
  trait DateRangeFormatter[T <: Temporal, F]:
    def format(dateRange: DateRange[T], formatter: F): (String, String)

  implicit val localDateRangeFormatter: DateRangeFormatter[LocalDate, DateTimeFormatter] =
    (dateRange: DateRange[LocalDate], formatter: DateTimeFormatter) =>
      (dateRange.from.format(formatter).nn, dateRange.to.format(formatter).nn)

  extension [T <: Temporal, F](self: DateRange[T])
    def format(formatter: F)(implicit
        dateRangeFormatter: DateRangeFormatter[T, F],
    ): (String, String) = dateRangeFormatter.format(self, formatter)
