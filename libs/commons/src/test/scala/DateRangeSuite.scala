package es.eriktorr.lambda4s

import DateRange.localDateTimeRangeWeekConverter.localDateRangeWeekConverter

import munit.FunSuite

import java.time.{LocalDate, Month}

final class DateRangeSuite extends FunSuite:
  test("should get the weeks covering a given date time range") {
    val dateRange = DateRange(
      LocalDate.of(2020, Month.DECEMBER, 15).nn.atStartOfDay().nn,
      LocalDate.of(2021, Month.FEBRUARY, 7).nn.atStartOfDay().nn,
    )
    assertEquals(
      dateRange.weeksCovering().sorted,
      List(
        DateRange(
          LocalDate.of(2020, Month.DECEMBER, 13).nn.atStartOfDay().nn,
          LocalDate.of(2020, Month.DECEMBER, 19).nn.atStartOfDay().nn,
        ),
        DateRange(
          LocalDate.of(2020, Month.DECEMBER, 20).nn.atStartOfDay().nn,
          LocalDate.of(2020, Month.DECEMBER, 26).nn.atStartOfDay().nn,
        ),
        DateRange(
          LocalDate.of(2020, Month.DECEMBER, 27).nn.atStartOfDay().nn,
          LocalDate.of(2021, Month.JANUARY, 2).nn.atStartOfDay().nn,
        ),
        DateRange(
          LocalDate.of(2021, Month.JANUARY, 3).nn.atStartOfDay().nn,
          LocalDate.of(2021, Month.JANUARY, 9).nn.atStartOfDay().nn,
        ),
        DateRange(
          LocalDate.of(2021, Month.JANUARY, 10).nn.atStartOfDay().nn,
          LocalDate.of(2021, Month.JANUARY, 16).nn.atStartOfDay().nn,
        ),
        DateRange(
          LocalDate.of(2021, Month.JANUARY, 17).nn.atStartOfDay().nn,
          LocalDate.of(2021, Month.JANUARY, 23).nn.atStartOfDay().nn,
        ),
        DateRange(
          LocalDate.of(2021, Month.JANUARY, 24).nn.atStartOfDay().nn,
          LocalDate.of(2021, Month.JANUARY, 30).nn.atStartOfDay().nn,
        ),
        DateRange(
          LocalDate.of(2021, Month.JANUARY, 31).nn.atStartOfDay().nn,
          LocalDate.of(2021, Month.FEBRUARY, 6).nn.atStartOfDay().nn,
        ),
        DateRange(
          LocalDate.of(2021, Month.FEBRUARY, 7).nn.atStartOfDay().nn,
          LocalDate.of(2021, Month.FEBRUARY, 13).nn.atStartOfDay().nn,
        ),
      ).sorted,
    )
  }

  test(
    "should get the weeks covering a given date time range matching the first and last day of the range",
  ) {
    val dateRange = DateRange(
      LocalDate.of(2020, Month.DECEMBER, 15).nn.atStartOfDay().nn,
      LocalDate.of(2021, Month.FEBRUARY, 7).nn.atStartOfDay().nn,
    )
    assertEquals(
      dateRange.weeksCovering(exactMatch = true).sorted,
      List(
        DateRange(
          LocalDate.of(2020, Month.DECEMBER, 15).nn.atStartOfDay().nn,
          LocalDate.of(2020, Month.DECEMBER, 19).nn.atStartOfDay().nn,
        ),
        DateRange(
          LocalDate.of(2020, Month.DECEMBER, 20).nn.atStartOfDay().nn,
          LocalDate.of(2020, Month.DECEMBER, 26).nn.atStartOfDay().nn,
        ),
        DateRange(
          LocalDate.of(2020, Month.DECEMBER, 27).nn.atStartOfDay().nn,
          LocalDate.of(2021, Month.JANUARY, 2).nn.atStartOfDay().nn,
        ),
        DateRange(
          LocalDate.of(2021, Month.JANUARY, 3).nn.atStartOfDay().nn,
          LocalDate.of(2021, Month.JANUARY, 9).nn.atStartOfDay().nn,
        ),
        DateRange(
          LocalDate.of(2021, Month.JANUARY, 10).nn.atStartOfDay().nn,
          LocalDate.of(2021, Month.JANUARY, 16).nn.atStartOfDay().nn,
        ),
        DateRange(
          LocalDate.of(2021, Month.JANUARY, 17).nn.atStartOfDay().nn,
          LocalDate.of(2021, Month.JANUARY, 23).nn.atStartOfDay().nn,
        ),
        DateRange(
          LocalDate.of(2021, Month.JANUARY, 24).nn.atStartOfDay().nn,
          LocalDate.of(2021, Month.JANUARY, 30).nn.atStartOfDay().nn,
        ),
        DateRange(
          LocalDate.of(2021, Month.JANUARY, 31).nn.atStartOfDay().nn,
          LocalDate.of(2021, Month.FEBRUARY, 6).nn.atStartOfDay().nn,
        ),
        DateRange(
          LocalDate.of(2021, Month.FEBRUARY, 7).nn.atStartOfDay().nn,
          LocalDate.of(2021, Month.FEBRUARY, 7).nn.atStartOfDay().nn,
        ),
      ).sorted,
    )
  }

  test("should get the weeks covering a given date range") {
    val dateRange =
      DateRange(LocalDate.of(2020, Month.DECEMBER, 15).nn, LocalDate.of(2021, Month.FEBRUARY, 7).nn)
    assertEquals(
      dateRange.weeksCovering().sorted,
      List(
        DateRange(
          LocalDate.of(2020, Month.DECEMBER, 13).nn,
          LocalDate.of(2020, Month.DECEMBER, 19).nn,
        ),
        DateRange(
          LocalDate.of(2020, Month.DECEMBER, 20).nn,
          LocalDate.of(2020, Month.DECEMBER, 26).nn,
        ),
        DateRange(
          LocalDate.of(2020, Month.DECEMBER, 27).nn,
          LocalDate.of(2021, Month.JANUARY, 2).nn,
        ),
        DateRange(
          LocalDate.of(2021, Month.JANUARY, 3).nn,
          LocalDate.of(2021, Month.JANUARY, 9).nn,
        ),
        DateRange(
          LocalDate.of(2021, Month.JANUARY, 10).nn,
          LocalDate.of(2021, Month.JANUARY, 16).nn,
        ),
        DateRange(
          LocalDate.of(2021, Month.JANUARY, 17).nn,
          LocalDate.of(2021, Month.JANUARY, 23).nn,
        ),
        DateRange(
          LocalDate.of(2021, Month.JANUARY, 24).nn,
          LocalDate.of(2021, Month.JANUARY, 30).nn,
        ),
        DateRange(
          LocalDate.of(2021, Month.JANUARY, 31).nn,
          LocalDate.of(2021, Month.FEBRUARY, 6).nn,
        ),
        DateRange(
          LocalDate.of(2021, Month.FEBRUARY, 7).nn,
          LocalDate.of(2021, Month.FEBRUARY, 13).nn,
        ),
      ).sorted,
    )
  }

  test(
    "should get the weeks covering a given date range matching the first and last day of the range",
  ) {
    val dateRange =
      DateRange(LocalDate.of(2020, Month.DECEMBER, 15).nn, LocalDate.of(2021, Month.FEBRUARY, 7).nn)
    assertEquals(
      dateRange.weeksCovering(exactMatch = true).sorted,
      List(
        DateRange(
          LocalDate.of(2020, Month.DECEMBER, 15).nn,
          LocalDate.of(2020, Month.DECEMBER, 19).nn,
        ),
        DateRange(
          LocalDate.of(2020, Month.DECEMBER, 20).nn,
          LocalDate.of(2020, Month.DECEMBER, 26).nn,
        ),
        DateRange(
          LocalDate.of(2020, Month.DECEMBER, 27).nn,
          LocalDate.of(2021, Month.JANUARY, 2).nn,
        ),
        DateRange(
          LocalDate.of(2021, Month.JANUARY, 3).nn,
          LocalDate.of(2021, Month.JANUARY, 9).nn,
        ),
        DateRange(
          LocalDate.of(2021, Month.JANUARY, 10).nn,
          LocalDate.of(2021, Month.JANUARY, 16).nn,
        ),
        DateRange(
          LocalDate.of(2021, Month.JANUARY, 17).nn,
          LocalDate.of(2021, Month.JANUARY, 23).nn,
        ),
        DateRange(
          LocalDate.of(2021, Month.JANUARY, 24).nn,
          LocalDate.of(2021, Month.JANUARY, 30).nn,
        ),
        DateRange(
          LocalDate.of(2021, Month.JANUARY, 31).nn,
          LocalDate.of(2021, Month.FEBRUARY, 6).nn,
        ),
        DateRange(
          LocalDate.of(2021, Month.FEBRUARY, 7).nn,
          LocalDate.of(2021, Month.FEBRUARY, 7).nn,
        ),
      ).sorted,
    )
  }
