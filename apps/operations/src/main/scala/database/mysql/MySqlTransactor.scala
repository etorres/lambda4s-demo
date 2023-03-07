package es.eriktorr.lambda4s
package database.mysql

import database.PromiseLikeSyntax.toIO
import database.{DatabaseConfiguration, IoQuery, Transactor}

import cats.effect.{IO, Resource}
import typings.promiseMysql.mod.{createConnection, Connection, ConnectionConfig}

import scala.concurrent.ExecutionContext
import scala.scalajs.js

final class MySqlTransactor private (connection: Connection)(using val executor: ExecutionContext)
    extends Transactor:
  override def query(expression: String): IoQuery = IoQuery(
    connection
      .query[js.Array[js.Object]](expression)
      .toIO,
  )

  override def update(expression: String): IO[Unit] =
    connection.query[Any](expression).toIO.flatMap(_ => IO.unit)

object MySqlTransactor:
  def of(
      databaseConfiguration: DatabaseConfiguration,
      executionContext: ExecutionContext,
  ): Resource[IO, Transactor] =
    given executor: ExecutionContext = executionContext
    for connection <- Resource
        .make(
          createConnection(
            ConnectionConfig()
              .setHost(databaseConfiguration.host.toString)
              .setUser(databaseConfiguration.user)
              .setPassword(databaseConfiguration.password.value)
              .setPort(databaseConfiguration.port.value.toDouble)
              .setDatabase(databaseConfiguration.database),
          ).toIO,
        )(connection => IO.blocking(connection.destroy()))
    yield new MySqlTransactor(connection)
