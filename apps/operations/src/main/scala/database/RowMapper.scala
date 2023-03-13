package es.eriktorr.lambda4s
package database

import database.DatabaseType.ColumnType
import database.DatabaseType.ColumnType.*
import refined.types.NonEmptyString

import org.typelevel.ci.CIString

import java.time.LocalDate
import scala.annotation.tailrec
import scala.deriving.Mirror
import scala.scalajs.js
import scala.util.{Failure, Success, Try}

trait RowMapper[A]:
  def from(rows: js.Array[js.Object]): List[A]

object RowMapper:
  inline given apply[A](using
      databaseType: DatabaseType[A],
      databaseTypeHints: DatabaseTypeHints,
      mirror: Mirror.ProductOf[A],
  ): RowMapper[A] = (rows: js.Array[js.Object]) =>
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
                (ColumnType.valueOf(columnType) match
                  case DateType =>
                    val jsDate = value.asInstanceOf[js.Date]
                    LocalDate
                      .of(
                        jsDate.getFullYear().toInt,
                        jsDate.getMonth().toInt + 1,
                        jsDate.getDate().toInt,
                      )
                  case EnumType =>
                    val jsString = value.asInstanceOf[String]
                    databaseTypeHints
                      .hintFor(columnName)
                      .map(_.apply(jsString))
                      .getOrElse(
                        throw IllegalStateException(s"No hint found for column: $columnName"),
                      )
                  case NonEmptyStringType =>
                    val jsString = value.asInstanceOf[String]
                    NonEmptyString
                      .fromString(jsString)
                      .getOrElse(
                        throw IllegalStateException(s"Unsupported value for column: $columnName"),
                      )
                  case DoubleType | IntType | StringType => value
                ) *: values
              }
              .getOrElse(throw IllegalStateException(s"Missing field: $columnName")),
          )
      val values = fillWith(fields, EmptyTuple)
      summon[Mirror.ProductOf[A]].fromProduct(values)
    }.toList
