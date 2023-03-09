package es.eriktorr.lambda4s
package database

import database.TestRow.Ranking.{First, Looser, Second, Third}
import database.TestRow.{testRowDatabaseTypeHints, Ranking}
import refined.types.NonEmptyString

import cats.data.NonEmptyList
import cats.effect.IO
import munit.CatsEffectSuite
import org.typelevel.ci.CIStringSyntax

import java.time.LocalDate
import scala.scalajs.js

final class IoQuerySuite extends CatsEffectSuite:
  test("should select an optional element from the given array") {
    IoQuery(
      IO.pure(
        js.Array(
          JsTestRow("Row 1", 43, 78.5d, new js.Date(2022, 2, 16), Third.toString, "ChangeMe"),
        ),
      ),
    )
      .option[TestRow]
      .assertEquals(
        Some(
          TestRow(
            "Row 1",
            43,
            78.5d,
            LocalDate.of(2022, 3, 16).nn,
            Third,
            NonEmptyString.unsafeFrom("ChangeMe"),
          ),
        ),
      )
  }

  test("should get none elements when the given array is empty") {
    IoQuery(IO.pure(js.Array())).option[TestRow].assertEquals(None)
  }

  test("should fail with an error when the given array has more than one element") {
    IoQuery(
      IO.pure(
        js.Array(
          JsTestRow("Row 1", 43, 78.5d, new js.Date(2022, 2, 16), First.toString, "ChangeMe"),
          JsTestRow("Row 2", 57, 12.0d, new js.Date(2019, 8, 17), Second.toString, "123456"),
        ),
      ),
    )
      .option[TestRow]
      .interceptMessage[IllegalArgumentException]("Multiple records found, one expected")
  }

  test("should select an optional element from the given array") {
    IoQuery(
      IO.pure(
        js.Array(
          JsTestRow("Row 1", 43, 78.5d, new js.Date(2022, 2, 16), First.toString, "ChangeMe"),
        ),
      ),
    )
      .orElse[TestRow](
        TestRow(
          "Default",
          1,
          0.0d,
          LocalDate.of(1995, 1, 1).nn,
          Looser,
          NonEmptyString.unsafeFrom("123456"),
        ),
      )
      .assertEquals(
        TestRow(
          "Row 1",
          43,
          78.5d,
          LocalDate.of(2022, 3, 16).nn,
          First,
          NonEmptyString.unsafeFrom("ChangeMe"),
        ),
      )
  }

  test("should return a default value when the given array is empty") {
    IoQuery(IO.pure(js.Array()))
      .orElse[TestRow](
        TestRow(
          "Default",
          1,
          0.0d,
          LocalDate.of(1995, 1, 1).nn,
          Looser,
          NonEmptyString.unsafeFrom("ChangeMe"),
        ),
      )
      .assertEquals(
        TestRow(
          "Default",
          1,
          0.0d,
          LocalDate.of(1995, 1, 1).nn,
          Looser,
          NonEmptyString.unsafeFrom("ChangeMe"),
        ),
      )
  }

  test("should fail with an error when the given array has more than one element") {
    IoQuery(
      IO.pure(
        js.Array(
          JsTestRow("Row 1", 43, 78.5d, new js.Date(2022, 2, 16), First.toString, "123456"),
          JsTestRow("Row 2", 57, 12.0d, new js.Date(2019, 8, 17), Second.toString, "654321"),
        ),
      ),
    )
      .orElse[TestRow](
        TestRow(
          "Default",
          1,
          0.0d,
          LocalDate.of(1995, 1, 1).nn,
          Looser,
          NonEmptyString.unsafeFrom("ChangeMe"),
        ),
      )
      .interceptMessage[IllegalArgumentException]("Multiple records found, one expected")
  }

  test("should select a list of elements") {
    IoQuery(
      IO.pure(
        js.Array(
          JsTestRow("Row 1", 43, 78.5d, new js.Date(2022, 2, 16), First.toString, "123456"),
          JsTestRow("Row 2", 57, 12.0d, new js.Date(2019, 8, 17), Second.toString, "ChangeMe"),
        ),
      ),
    )
      .list[TestRow]
      .assertEquals(
        List(
          TestRow(
            "Row 1",
            43,
            78.5d,
            LocalDate.of(2022, 3, 16).nn,
            First,
            NonEmptyString.unsafeFrom("123456"),
          ),
          TestRow(
            "Row 2",
            57,
            12.0d,
            LocalDate.of(2019, 9, 17).nn,
            Second,
            NonEmptyString.unsafeFrom("ChangeMe"),
          ),
        ),
      )
  }

  test("should select a non-empty list of elements") {
    IoQuery(
      IO.pure(
        js.Array(
          JsTestRow("Row 1", 43, 78.5d, new js.Date(2022, 2, 16), First.toString, "ChangeMe"),
          JsTestRow("Row 2", 57, 12.0d, new js.Date(2019, 8, 17), Second.toString, "54321"),
        ),
      ),
    )
      .nonEmptyList[TestRow]
      .assertEquals(
        NonEmptyList.fromListUnsafe(
          List(
            TestRow(
              "Row 1",
              43,
              78.5d,
              LocalDate.of(2022, 3, 16).nn,
              First,
              NonEmptyString.unsafeFrom("ChangeMe"),
            ),
            TestRow(
              "Row 2",
              57,
              12.0d,
              LocalDate.of(2019, 9, 17).nn,
              Second,
              NonEmptyString.unsafeFrom("54321"),
            ),
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
      IO.pure(
        js.Array(JsTestRow("Row 1", 43, 78.5d, new js.Date(2022, 2, 16), First.toString, "Secret")),
      ),
    )
      .unique[TestRow]
      .assertEquals(
        TestRow(
          "Row 1",
          43,
          78.5d,
          LocalDate.of(2022, 3, 16).nn,
          First,
          NonEmptyString.unsafeFrom("Secret"),
        ),
      )
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
          JsTestRow("Row 1", 43, 78.5d, new js.Date(2022, 2, 16), First.toString, "ChangeMe"),
          JsTestRow("Row 2", 57, 12.0d, new js.Date(2019, 8, 17), Second.toString, "Secret"),
        ),
      ),
    )
      .unique[TestRow]
      .interceptMessage[IllegalArgumentException]("Multiple records found, one expected")
  }
