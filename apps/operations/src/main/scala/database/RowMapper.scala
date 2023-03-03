package es.eriktorr.lambda4s
package database

import org.typelevel.ci.CIString

import java.time.LocalDate
import scala.annotation.tailrec
import scala.deriving.Mirror
import scala.scalajs.js

trait RowMapper[A]:
  def from(rows: js.Array[js.Object]): List[A]

object RowMapper:
  def from[A](
      rows: js.Array[js.Object],
  )(using
      databaseType: DatabaseType[A],
      mirror: Mirror.ProductOf[A],
  ): List[A] =
    val fields = databaseType.columns.map { (columnName, columnType) =>
      CIString(columnName) -> columnType
    }.reverse

    rows.map { row =>
      val rowMap = js.Object.entries(row).map(t => (CIString(t._1), t._2)).toMap

      @tailrec
      def fillWith(fields: List[(CIString, String)], values: Tuple): Tuple = fields match
        case Nil => values
        case ::((columnName, columnType), next) =>
          fillWith(
            next,
            rowMap
              .get(columnName)
              .map { value =>
                (columnType match
                  case "DateType" =>
                    val jsDate = value.asInstanceOf[js.Date]
                    LocalDate.of(
                      jsDate.getFullYear().toInt,
                      jsDate.getMonth().toInt + 1,
                      jsDate.getDate().toInt,
                    )
                  case _ => value
                ) *: values
              }
              .getOrElse(throw new IllegalStateException(s"Missing field: $columnName")),
          )

      val values = fillWith(fields, EmptyTuple)
      summon[Mirror.ProductOf[A]].fromProduct(values)
    }.toList
