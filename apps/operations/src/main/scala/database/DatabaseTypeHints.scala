package es.eriktorr.lambda4s
package database

import org.typelevel.ci.CIString

final case class DatabaseTypeHints(hints: Map[CIString, String => Any]):
  def hintFor(column: CIString): Option[String => Any] = hints.get(column)

object DatabaseTypeHints:
  given DatabaseTypeHints = DatabaseTypeHints(Map.empty)
