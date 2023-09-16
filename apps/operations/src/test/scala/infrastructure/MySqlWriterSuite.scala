package es.eriktorr.lambda4s
package infrastructure

import cats.effect.IO
import cats.implicits.toFoldableOps

abstract class MySqlWriterSuite[A](testTransactor: MySqlTestTransactor):
  def add(rows: List[A], rowToQuery: A => String): IO[Unit] =
    rows.traverse_(row => testTransactor.update(rowToQuery(row)))
