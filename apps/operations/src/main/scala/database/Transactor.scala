package es.eriktorr.lambda4s
package database

import cats.effect.IO

trait Transactor:
  def query(expression: String): IoQuery
  def update(expression: String): IO[Unit]
