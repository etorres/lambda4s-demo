package es.eriktorr.lambda4s
package infrastructure

import database.IoQuery.IoQuerySyntax.IoQueryOps
import database.RowMapper
import database.mysql.MySqlTransactor
import infrastructure.MySqlWriterSuite.MysqlTable

import cats.effect.IO
import cats.implicits.toFoldableOps

import scala.annotation.nowarn
import scala.concurrent.ExecutionContext
import scala.scalajs.js

abstract class MySqlWriterSuite[A](
    databaseTestConfiguration: DatabaseTestConfiguration,
    executionContext: ExecutionContext,
):
  def add(rows: List[A], rowToQuery: A => String): IO[Unit] =
    MySqlTransactor
      .of(databaseTestConfiguration.config, executionContext)
      .use(connection =>
        for
          _ <- connection.update("SET FOREIGN_KEY_CHECKS = 0")
          tables <- connection
            .query(
              s"""SELECT table_name
                 | FROM information_schema.tables
                 | WHERE table_schema = '${databaseTestConfiguration.database}'""".stripMargin,
            )
            .list[MysqlTable]
          _ <- tables.traverse_(table => connection.update(s"TRUNCATE TABLE ${table.table_name}"))
          _ <- connection.update("SET FOREIGN_KEY_CHECKS = 1")
          _ <- rows.traverse_(row => connection.update(rowToQuery(row)))
        yield (),
      )

object MySqlWriterSuite:
  final private case class MysqlTable(table_name: String)

  @nowarn("msg=Declaration is never used") // IntelliJ IDEA
  private given mysqlTableRowMapper: RowMapper[MysqlTable] with
    override def from(rows: js.Array[js.Object]): List[MysqlTable] =
      RowMapper.from[MysqlTable](rows)
