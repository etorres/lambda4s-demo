package es.eriktorr.lambda4s
package infrastructure

import database.mysql.MySqlTransactor
import database.{IoQuery, RowMapper, Transactor}

import cats.effect.{IO, Resource}
import cats.implicits.toFoldableOps

import scala.annotation.nowarn
import scala.concurrent.ExecutionContext
import scala.scalajs.js

final class MySqlTestTransactor private (val transactor: Transactor) extends Transactor:
  override def query(expression: String): IoQuery = transactor.query(expression)

  override def update(expression: String): IO[Unit] = transactor.update(expression)

object MySqlTestTransactor:
  def of(
      databaseTestConfiguration: DatabaseTestConfiguration,
      executionContext: ExecutionContext,
  ): Resource[IO, MySqlTestTransactor] = for
    transactor <- MySqlTransactor.of(databaseTestConfiguration.config, executionContext)
    _ <- Resource.eval(for
      _ <- transactor.update("SET FOREIGN_KEY_CHECKS = 0")
      tables <- transactor
        .query(
          s"""SELECT table_name
             | FROM information_schema.tables
             | WHERE table_schema = '${databaseTestConfiguration.database}'
             |   AND table_type = 'BASE TABLE'""".stripMargin,
        )
        .list[MysqlTable]
      _ <- tables.traverse_(table => transactor.update(s"TRUNCATE TABLE ${table.table_name}"))
      _ <- transactor.update("SET FOREIGN_KEY_CHECKS = 1")
    yield ())
  yield MySqlTestTransactor(transactor)

  final private case class MysqlTable(table_name: String)

  @nowarn("msg=Declaration is never used") // IntelliJ IDEA
  private given RowMapper[MysqlTable] with
    override def from(rows: js.Array[js.Object]): List[MysqlTable] =
      RowMapper.from[MysqlTable](rows)
