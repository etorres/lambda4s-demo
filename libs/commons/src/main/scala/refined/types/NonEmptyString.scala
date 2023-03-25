package es.eriktorr.lambda4s
package refined.types

import cats.Show

opaque type NonEmptyString <: String = String

object NonEmptyString:
  def from(value: String): Option[NonEmptyString] =
    if value.nonEmpty then Some(value) else None

  def unsafeFrom(value: String): NonEmptyString =
    from(value).getOrElse(throw IllegalArgumentException(errorMessage))

  extension (nonEmptyString: NonEmptyString) def asString: String = nonEmptyString

  given Show[NonEmptyString] = Show.fromToString

  private inline val errorMessage = "Value cannot be empty"

  inline def const(inline value: String): NonEmptyString =
    scala.compiletime.requireConst(value)
    inline if nonEmptyConst(value) then value
    else scala.compiletime.error(errorMessage)

  import scala.quoted.{Expr, Quotes}

  private transparent inline def nonEmptyConst(inline value: String): Boolean = ${
    nonEmptyConstImpl('{ value })
  }

  private def nonEmptyConstImpl(value: Expr[String])(using Quotes): Expr[Boolean] =
    val res = value.valueOrAbort.nonEmpty
    Expr(res)
