package es.eriktorr.lambda4s

import java.time.format.DateTimeFormatter
import java.time.temporal.{Temporal, TemporalAdjusters, WeekFields}
import java.time.{LocalDate, LocalDateTime}
import java.util.Locale
import scala.annotation.tailrec

final case class DateRange[T <: Temporal](from: T, to: T)

object DateRange:
  import scala.language.unsafeNulls

  trait DateRangeFormatter[T <: Temporal, F]:
    def format(dateRange: DateRange[T], formatter: F): (String, String)

  given localDateTimeRangeFormatter: DateRangeFormatter[LocalDateTime, DateTimeFormatter] with
    override def format(
        dateRange: DateRange[LocalDateTime],
        formatter: DateTimeFormatter,
    ): (String, String) = (dateRange.from.format(formatter), dateRange.to.format(formatter))

  given localDateRangeFormatter: DateRangeFormatter[LocalDate, DateTimeFormatter] with
    override def format(
        dateRange: DateRange[LocalDate],
        formatter: DateTimeFormatter,
    ): (String, String) = (dateRange.from.format(formatter), dateRange.to.format(formatter))

  extension [T <: Temporal, F](self: DateRange[T])
    def format(formatter: F)(using dateRangeFormatter: DateRangeFormatter[T, F]): (String, String) =
      dateRangeFormatter.format(self, formatter)

  trait WeekConverter[T <: Temporal]:
    def weeksCovering(dateRange: DateRange[T], exactMatch: Boolean): List[DateRange[T]]

  given localDateTimeRangeWeekConverter: WeekConverter[LocalDateTime] with
    override def weeksCovering(
        dateRange: DateRange[LocalDateTime],
        exactMatch: Boolean,
    ): List[DateRange[LocalDateTime]] =
      def min(x: LocalDateTime, y: LocalDateTime) = if x.isBefore(y) then x else y
      def max(x: LocalDateTime, y: LocalDateTime) = if x.isAfter(y) then x else y

      @tailrec
      def weeksCovering(
          weekStart: LocalDateTime,
          weeks: List[DateRange[LocalDateTime]],
      ): List[DateRange[LocalDateTime]] =
        if weekStart.isAfter(dateRange.to) then weeks
        else
          val weekEnd = weekStart.plusDays(6L)
          weeksCovering(
            weekStart.plusDays(7L),
            DateRange(
              if exactMatch then max(weekStart, dateRange.from) else weekStart,
              if exactMatch then min(weekEnd, dateRange.to) else weekEnd,
            ) :: weeks,
          )

      weeksCovering(
        dateRange.from
          .`with`(TemporalAdjusters.previousOrSame(WeekFields.of(Locale.US).getFirstDayOfWeek))
          .asInstanceOf[LocalDateTime],
        List.empty,
      )

    given localDateRangeWeekConverter: WeekConverter[LocalDate] with
      override def weeksCovering(
          dateRange: DateRange[LocalDate],
          exactMatch: Boolean,
      ): List[DateRange[LocalDate]] =
        val localDateTimeRange = DateRange(dateRange.from.atStartOfDay, dateRange.to.atStartOfDay)
        localDateTimeRangeWeekConverter.weeksCovering(localDateTimeRange, exactMatch).map { week =>
          DateRange(week.from.toLocalDate, week.to.toLocalDate)
        }

  extension [T <: Temporal](self: DateRange[T])
    def weeksCovering(exactMatch: Boolean = false)(using
        weekConverter: WeekConverter[T],
    ): List[DateRange[T]] =
      weekConverter.weeksCovering(self, exactMatch)

  given localDateTimeRangeOrdering: Ordering[DateRange[LocalDateTime]] =
    Ordering.by[DateRange[LocalDateTime], LocalDateTime](_.from)

  given localDateRangeOrdering: Ordering[DateRange[LocalDate]] =
    Ordering.by[DateRange[LocalDate], LocalDate](_.from)
