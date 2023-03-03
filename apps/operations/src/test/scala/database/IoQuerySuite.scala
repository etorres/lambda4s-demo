package es.eriktorr.lambda4s
package database

import database.TestRow.Ranking.{First, Looser, Second, Third}
import database.TestRow.{testRowDatabaseTypeHints, Ranking}

import cats.data.NonEmptyList
import cats.effect.IO
import munit.CatsEffectSuite
import org.typelevel.ci.CIStringSyntax

import java.time.LocalDate
import scala.scalajs.js

final class IoQuerySuite extends CatsEffectSuite:
  test("should select an optional element from the given array") {
    IoQuery(
      IO.pure(js.Array(JsTestRow("Row 1", 43, 78.5d, new js.Date(2022, 2, 16), Third.toString))),
    )
      .option[TestRow]
      .assertEquals(Some(TestRow("Row 1", 43, 78.5d, LocalDate.of(2022, 3, 16).nn, Third)))
  }

  test("should fail with an error when the given array is empty") {
    IoQuery(IO.pure(js.Array()))
      .option[TestRow]
      .interceptMessage[IllegalArgumentException]("No records found")
  }

  test("should fail with an error when the given array has more than one element") {
    IoQuery(
      IO.pure(
        js.Array(
          JsTestRow("Row 1", 43, 78.5d, new js.Date(2022, 2, 16), First.toString),
          JsTestRow("Row 2", 57, 12.0d, new js.Date(2019, 8, 17), Second.toString),
        ),
      ),
    )
      .option[TestRow]
      .interceptMessage[IllegalArgumentException]("Multiple records found, one expected")
  }

  test("should select an optional element from the given array") {
    IoQuery(
      IO.pure(js.Array(JsTestRow("Row 1", 43, 78.5d, new js.Date(2022, 2, 16), First.toString))),
    )
      .orElse[TestRow](TestRow("Default", 1, 0.0d, LocalDate.of(1995, 1, 1).nn, Looser))
      .assertEquals(TestRow("Row 1", 43, 78.5d, LocalDate.of(2022, 3, 16).nn, First))
  }

  test("should return a default value when the given array is empty") {
    IoQuery(IO.pure(js.Array()))
      .orElse[TestRow](TestRow("Default", 1, 0.0d, LocalDate.of(1995, 1, 1).nn, Looser))
      .assertEquals(TestRow("Default", 1, 0.0d, LocalDate.of(1995, 1, 1).nn, Looser))
  }

  test("should fail with an error when the given array has more than one element") {
    IoQuery(
      IO.pure(
        js.Array(
          JsTestRow("Row 1", 43, 78.5d, new js.Date(2022, 2, 16), First.toString),
          JsTestRow("Row 2", 57, 12.0d, new js.Date(2019, 8, 17), Second.toString),
        ),
      ),
    )
      .orElse[TestRow](TestRow("Default", 1, 0.0d, LocalDate.of(1995, 1, 1).nn, Looser))
      .interceptMessage[IllegalArgumentException]("Multiple records found, one expected")
  }

  test("should select a list of elements") {
    IoQuery(
      IO.pure(
        js.Array(
          JsTestRow("Row 1", 43, 78.5d, new js.Date(2022, 2, 16), First.toString),
          JsTestRow("Row 2", 57, 12.0d, new js.Date(2019, 8, 17), Second.toString),
        ),
      ),
    )
      .list[TestRow]
      .assertEquals(
        List(
          TestRow("Row 1", 43, 78.5d, LocalDate.of(2022, 3, 16).nn, First),
          TestRow("Row 2", 57, 12.0d, LocalDate.of(2019, 9, 17).nn, Second),
        ),
      )
  }

  test("should select a non-empty list of elements") {
    IoQuery(
      IO.pure(
        js.Array(
          JsTestRow("Row 1", 43, 78.5d, new js.Date(2022, 2, 16), First.toString),
          JsTestRow("Row 2", 57, 12.0d, new js.Date(2019, 8, 17), Second.toString),
        ),
      ),
    )
      .nonEmptyList[TestRow]
      .assertEquals(
        NonEmptyList.fromListUnsafe(
          List(
            TestRow("Row 1", 43, 78.5d, LocalDate.of(2022, 3, 16).nn, First),
            TestRow("Row 2", 57, 12.0d, LocalDate.of(2019, 9, 17).nn, Second),
          ),
        ),
      )
  }

  test("should fail with an error when the given array is empty") {
    IoQuery(IO.pure(js.Array()))
      .nonEmptyList[TestRow]
      .interceptMessage[IllegalArgumentException]("No records found")
  }

  test("should select a mandatory element from the given array") {
    IoQuery(
      IO.pure(js.Array(JsTestRow("Row 1", 43, 78.5d, new js.Date(2022, 2, 16), First.toString))),
    )
      .unique[TestRow]
      .assertEquals(TestRow("Row 1", 43, 78.5d, LocalDate.of(2022, 3, 16).nn, First))
  }

  test("should fail with an error when the given array is empty") {
    IoQuery(IO.pure(js.Array()))
      .unique[TestRow]
      .interceptMessage[IllegalArgumentException]("No records found")
  }

  test("should fail with an error when the given array has more than one element") {
    IoQuery(
      IO.pure(
        js.Array(
          JsTestRow("Row 1", 43, 78.5d, new js.Date(2022, 2, 16), First.toString),
          JsTestRow("Row 2", 57, 12.0d, new js.Date(2019, 8, 17), Second.toString),
        ),
      ),
    )
      .unique[TestRow]
      .interceptMessage[IllegalArgumentException]("Multiple records found, one expected")
  }
