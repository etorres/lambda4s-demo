package es.eriktorr.lambda4s
package database

import cats.data.NonEmptyList
import cats.effect.IO

import scala.scalajs.js

final case class IoQuery(query: IO[js.Array[js.Object]])

/** IO query operations.
  *
  * @see
  *   [[https://tpolecat.github.io/doobie/docs/04-Selecting.html#reading-rows-into-collections Reading Rows into Collections]]
  */
object IoQuery:
  extension (self: IoQuery)
    def option[A](using rowMapper: RowMapper[A]): IO[Option[A]] = list[A].flatMap {
      case ::(head, Nil) => IO.some(head)
      case Nil => IO.none
      case _ =>
        IO.raiseError(new IllegalArgumentException("Multiple records found, one expected"))
    }

    def orElse[A](defaultValue: A)(using rowMapper: RowMapper[A]): IO[A] = list[A].flatMap {
      case ::(head, Nil) => IO.pure(head)
      case Nil => IO.pure(defaultValue)
      case _ =>
        IO.raiseError(new IllegalArgumentException("Multiple records found, one expected"))
    }

    def list[A](using rowMapper: RowMapper[A]): IO[List[A]] = self.query.map(rowMapper.from)

    def nonEmptyList[A](using rowMapper: RowMapper[A]): IO[NonEmptyList[A]] =
      list[A].flatMap(
        NonEmptyList
          .fromList(_)
          .fold(IO.raiseError[NonEmptyList[A]](new IllegalArgumentException("No records found")))(
            IO.pure,
          ),
      )

    def unique[A](using rowMapper: RowMapper[A]): IO[A] =
      option[A].flatMap(
        _.fold(IO.raiseError(new IllegalArgumentException("No records found")))(IO.pure),
      )
