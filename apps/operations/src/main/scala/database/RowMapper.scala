package es.eriktorr.lambda4s
package database

import shapeless3.deriving.Labelling

import scala.annotation.tailrec
import scala.deriving.Mirror
import scala.scalajs.js

trait RowMapper[A]:
  def from(rows: js.Array[js.Object]): List[A]

object RowMapper:
  def from[A](
      rows: js.Array[js.Object],
  )(using labelling: Labelling[A], mirror: Mirror.ProductOf[A]): List[A] =
    val fieldNames = labelling.elemLabels.reverse.toList

    rows.map { row =>
      val rowMap = js.Object.entries(row).map(t => (t._1, t._2)).toMap

      @tailrec
      def fillWith(fieldNames: List[String], values: Tuple): Tuple = fieldNames match
        case Nil => values
        case ::(head, next) =>
          fillWith(
            next,
            rowMap
              .get(head)
              .map(_ *: values)
              .getOrElse(throw new IllegalStateException(s"Missing field: $head")),
          )

      val values = fillWith(fieldNames, EmptyTuple)
      summon[Mirror.ProductOf[A]].fromProduct(values)
    }.toList
