package es.eriktorr.lambda4s
package database

import database.IoQuery.IoQuerySyntax.IoQueryOps

import cats.data.NonEmptyList
import cats.effect.IO
import munit.CatsEffectSuite

import scala.scalajs.js

final class IoQuerySuite extends CatsEffectSuite:
  test("should select an optional element from the given array") {
    IoQuery(IO.pure(js.Array(JsTestRow("Row 1", 43, 78.5d))))
      .option[TestRow]
      .assertEquals(Some(TestRow("Row 1", 43, 78.5d)))
  }

  test("should fail with an error when the given array is empty") {
    IoQuery(IO.pure(js.Array()))
      .option[TestRow]
      .interceptMessage[IllegalArgumentException]("No records found")
  }

  test("should fail with an error when the given array has more than one element") {
    IoQuery(IO.pure(js.Array(JsTestRow("Row 1", 43, 78.5d), JsTestRow("Row 2", 57, 12.0d))))
      .option[TestRow]
      .interceptMessage[IllegalArgumentException]("Multiple records found, one expected")
  }

  test("should select an optional element from the given array") {
    IoQuery(IO.pure(js.Array(JsTestRow("Row 1", 43, 78.5d))))
      .orElse[TestRow](TestRow("Default", 1, 0.0d))
      .assertEquals(TestRow("Row 1", 43, 78.5d))
  }

  test("should return a default value when the given array is empty") {
    IoQuery(IO.pure(js.Array()))
      .orElse[TestRow](TestRow("Default", 1, 0.0d))
      .assertEquals(TestRow("Default", 1, 0.0d))
  }

  test("should fail with an error when the given array has more than one element") {
    IoQuery(IO.pure(js.Array(JsTestRow("Row 1", 43, 78.5d), JsTestRow("Row 2", 57, 12.0d))))
      .orElse[TestRow](TestRow("Default", 1, 0.0d))
      .interceptMessage[IllegalArgumentException]("Multiple records found, one expected")
  }

  test("should select a list of elements") {
    IoQuery(IO.pure(js.Array(JsTestRow("Row 1", 43, 78.5d), JsTestRow("Row 2", 57, 12.0d))))
      .list[TestRow]
      .assertEquals(List(TestRow("Row 1", 43, 78.5d), TestRow("Row 2", 57, 12.0d)))
  }

  test("should select a non-empty list of elements") {
    IoQuery(IO.pure(js.Array(JsTestRow("Row 1", 43, 78.5d), JsTestRow("Row 2", 57, 12.0d))))
      .nonEmptyList[TestRow]
      .assertEquals(
        NonEmptyList.fromListUnsafe(List(TestRow("Row 1", 43, 78.5d), TestRow("Row 2", 57, 12.0d))),
      )
  }

  test("should fail with an error when the given array is empty") {
    IoQuery(IO.pure(js.Array()))
      .nonEmptyList[TestRow]
      .interceptMessage[IllegalArgumentException]("No records found")
  }

  test("should select a mandatory element from the given array") {
    IoQuery(IO.pure(js.Array(JsTestRow("Row 1", 43, 78.5d))))
      .unique[TestRow]
      .assertEquals(TestRow("Row 1", 43, 78.5d))
  }

  test("should fail with an error when the given array is empty") {
    IoQuery(IO.pure(js.Array()))
      .unique[TestRow]
      .interceptMessage[IllegalArgumentException]("No records found")
  }

  test("should fail with an error when the given array has more than one element") {
    IoQuery(IO.pure(js.Array(JsTestRow("Row 1", 43, 78.5d), JsTestRow("Row 2", 57, 12.0d))))
      .unique[TestRow]
      .interceptMessage[IllegalArgumentException]("Multiple records found, one expected")
  }
