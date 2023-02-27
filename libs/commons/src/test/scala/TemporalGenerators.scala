package es.eriktorr.lambda4s

import com.fortysevendeg.scalacheck.datetime.GenDateTime.genDateTimeWithinRange
import com.fortysevendeg.scalacheck.datetime.YearRange
import com.fortysevendeg.scalacheck.datetime.instances.jdk8.*
import com.fortysevendeg.scalacheck.datetime.jdk8.ArbitraryJdk8.*
import com.fortysevendeg.scalacheck.datetime.jdk8.granularity.days
import org.scalacheck.Gen

import java.time.temporal.ChronoUnit
import java.time.{Duration, LocalDate}

object TemporalGenerators:
  import scala.language.unsafeNulls

  private given yearRange: YearRange = YearRange.between(1990, 2060)

  val localDateRangeGen: Gen[DateRange[LocalDate]] = for
    from <- arbLocalDateTimeJdk8.arbitrary
    to <- genDateTimeWithinRange(from.plusDays(1L), Duration.ofDays(365L))
  yield DateRange(from.toLocalDate, to.toLocalDate)

  def outOf(dateRange: DateRange[LocalDate]): Gen[LocalDate] =
    Gen.frequency(
      1 -> within(DateRange(dateRange.from.minusYears(1L), dateRange.from.minusDays(1L))),
      1 -> within(DateRange(dateRange.to.plusDays(1L), dateRange.to.plusYears(1L))),
    )

  def within(dateRange: DateRange[LocalDate]): Gen[LocalDate] =
    genDateTimeWithinRange(
      dateRange.from.atStartOfDay(),
      Duration.ofDays(ChronoUnit.DAYS.between(dateRange.from, dateRange.to)),
    ).map(_.toLocalDate)
