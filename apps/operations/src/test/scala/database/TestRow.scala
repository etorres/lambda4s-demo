package es.eriktorr.lambda4s
package database

import scala.scalajs.js

final case class TestRow(name: String, value: Int, price: Double)

object TestRow:
  given testRowMapper: RowMapper[TestRow] = (rows: js.Array[js.Object]) =>
    RowMapper.from[TestRow](rows)
