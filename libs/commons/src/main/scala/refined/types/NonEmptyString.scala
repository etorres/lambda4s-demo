package es.eriktorr.lambda4s
package refined.types

import cats.Show

import scala.compiletime.asMatchable
import scala.util.hashing.MurmurHash3

final class NonEmptyString private (val value: String) extends Product with Serializable:
  override def toString: String = value
  override def hashCode: Int = MurmurHash3.productHash(this, productPrefix.hashCode)
  override def canEqual(other: Any): Boolean = other.isInstanceOf[NonEmptyString]

  override def equals(other: Any): Boolean = other.asMatchable match
    case that: NonEmptyString => value == that.value
    case _ => false
  override def productArity: Int = 1
  override def productElement(n: Int): Any =
    if n == 0 then value else throw IndexOutOfBoundsException()

object NonEmptyString:
  def fromString(value: String): Option[NonEmptyString] =
    if value.nonEmpty then Some(NonEmptyString(value)) else None

  def unsafeFrom(value: String): NonEmptyString =
    fromString(value).getOrElse(throw IllegalArgumentException("Invalid value found"))

  def unapply(nonEmptyString: NonEmptyString): Option[String] = Some(nonEmptyString.value)

  given Show[NonEmptyString] = Show.fromToString[NonEmptyString]
