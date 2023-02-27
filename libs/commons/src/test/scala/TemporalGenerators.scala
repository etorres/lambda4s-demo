package es.eriktorr.lambda4s

import com.fortysevendeg.scalacheck.datetime.GenDateTime.genDateTimeWithinRange
import com.fortysevendeg.scalacheck.datetime.YearRange
import com.fortysevendeg.scalacheck.datetime.instances.jdk8.*
import com.fortysevendeg.scalacheck.datetime.jdk8.ArbitraryJdk8.*
import com.fortysevendeg.scalacheck.datetime.jdk8.granularity.days
import org.scalacheck.Gen

import java.time.temporal.ChronoUnit
import java.time.{Duration, LocalDate, LocalDateTime}

object TemporalGenerators:
  import scala.language.unsafeNulls

  private given yearRange: YearRange = YearRange.between(1990, 2060)

  val localDateTimeRangeGen: Gen[DateRange[LocalDateTime]] = for
    from <- arbLocalDateTimeJdk8.arbitrary
    to <- genDateTimeWithinRange(from.plusDays(1L), Duration.ofDays(365L))
  yield DateRange(from, to)

  val localDateRangeGen: Gen[DateRange[LocalDate]] = localDateTimeRangeGen.map(localDateTimeRange =>
    DateRange(localDateTimeRange.from.toLocalDate, localDateTimeRange.to.toLocalDate),
  )

  def after(localDateTime: LocalDateTime): Gen[LocalDateTime] =
    withinLocalDateTimeRange(DateRange(localDateTime.plusDays(1L), localDateTime.plusYears(1L)))

  def after(localDate: LocalDate): Gen[LocalDate] =
    withinLocalDateRange(DateRange(localDate.plusDays(1L), localDate.plusYears(1L)))

  def outOfLocalDateTimeRange(localDateTimeRange: DateRange[LocalDateTime]): Gen[LocalDateTime] =
    Gen.frequency(
      1 -> withinLocalDateTimeRange(
        DateRange(localDateTimeRange.from.minusYears(1L), localDateTimeRange.from.minusDays(1L)),
      ),
      1 -> withinLocalDateTimeRange(
        DateRange(localDateTimeRange.to.plusDays(1L), localDateTimeRange.to.plusYears(1L)),
      ),
    )

  def outOfLocalDateRange(dateRange: DateRange[LocalDate]): Gen[LocalDate] =
    Gen.frequency(
      1 -> withinLocalDateRange(
        DateRange(dateRange.from.minusYears(1L), dateRange.from.minusDays(1L)),
      ),
      1 -> withinLocalDateRange(DateRange(dateRange.to.plusDays(1L), dateRange.to.plusYears(1L))),
    )

  def withinLocalDateTimeRange(localDateTimeRange: DateRange[LocalDateTime]): Gen[LocalDateTime] =
    genDateTimeWithinRange(
      localDateTimeRange.from,
      Duration.ofDays(ChronoUnit.DAYS.between(localDateTimeRange.from, localDateTimeRange.to)),
    )

  def withinLocalDateRange(dateRange: DateRange[LocalDate]): Gen[LocalDate] =
    genDateTimeWithinRange(
      dateRange.from.atStartOfDay(),
      Duration.ofDays(ChronoUnit.DAYS.between(dateRange.from, dateRange.to)),
    ).map(_.toLocalDate)
