package es.eriktorr.lambda4s
package database

import database.TestRow.Ranking
import refined.types.NonEmptyString

import org.typelevel.ci.CIStringSyntax

import java.time.LocalDate

final case class TestRow(
    address: String,
    zipcode: Int,
    price: Double,
    lastUpdate: LocalDate,
    ranking: Ranking,
    password: NonEmptyString,
)

object TestRow:
  enum Ranking:
    case First, Second, Third, Looser

  given testRowDatabaseTypeHints: DatabaseTypeHints = DatabaseTypeHints(
    Map((ci"ranking", (ranking: String) => Ranking.valueOf(ranking).asInstanceOf[Any])),
  )
