package es.eriktorr.lambda4s
package database

import munit.FunSuite

import scala.scalajs.js

final class RowMapperSuite extends FunSuite:
  test("should map javascript object to scala case class") {
    val actual = RowMapper[TestRow].from(
      js.Array(JsTestRow("Row 1", 43, 78.5d), JsTestRow("Row 2", 57, 12.0d)),
    )
    val expected = List(TestRow("Row 1", 43, 78.5d), TestRow("Row 2", 57, 12.0d))
    assert(actual == expected)
  }
