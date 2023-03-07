package es.eriktorr.lambda4s
package database

import database.TestRow.Ranking.{First, Third}
import database.TestRow.{testRowDatabaseTypeHints, Ranking}
import refined.types.NonEmptyString

import munit.FunSuite

import java.time.LocalDate
import scala.scalajs.js

final class RowMapperSuite extends FunSuite:
  test("should map javascript object to scala case class") {
    val actual = RowMapper[TestRow].from(
      js.Array(
        JsTestRow("Row 1", 43, 78.5d, new js.Date(2022, 2, 16), Third.toString, "ChangeMe"),
        JsTestRow("Row 2", 57, 12.0d, new js.Date(2012, 10, 26), First.toString, "654321"),
      ),
    )
    val expected = List(
      TestRow(
        "Row 1",
        43,
        78.5d,
        LocalDate.of(2022, 3, 16).nn,
        Third,
        NonEmptyString.unsafeFrom("ChangeMe"),
      ),
      TestRow(
        "Row 2",
        57,
        12.0d,
        LocalDate.of(2012, 11, 26).nn,
        First,
        NonEmptyString.unsafeFrom("654321"),
      ),
    )
    assert(actual == expected)
  }
