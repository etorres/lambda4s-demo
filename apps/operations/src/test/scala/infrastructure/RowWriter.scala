package es.eriktorr.lambda4s
package infrastructure

import cats.effect.IO

trait RowWriter[R]:
  def add(rows: List[R]): IO[Unit]
