package es.eriktorr.lambda4s
package database

import munit.FunSuite

import java.time.LocalDate

final class DatabaseTypeSuite extends FunSuite:
  test("should get all fields from a casa class with their corresponding database types") {
    val databaseTypes = DatabaseType[TestRow]
    assert(
      databaseTypes == DatabaseType(
        List(
          "address" -> "StringType",
          "zipcode" -> "IntType",
          "price" -> "DoubleType",
          "lastUpdate" -> "DateType",
          "ranking" -> "EnumType",
        ),
      ),
    )
  }
