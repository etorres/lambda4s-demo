package es.eriktorr.lambda4s
package infrastructure

import database.DatabaseConfiguration

import com.comcast.ip4s.{host, port}

enum DatabaseTestConfiguration(val config: DatabaseConfiguration):
  import DatabaseTestConfiguration.mySqlTest

  def database: String = config.database

  case SakilaMySqlTest
      extends DatabaseTestConfiguration(
        DatabaseTestConfiguration.mySqlTest.copy(database = "sakila"),
      )

object DatabaseTestConfiguration:
  final private lazy val mySqlTest = DatabaseConfiguration(
    database = "<database>",
    host = host"mysql.test",
    password = "changeMe",
    port = port"3306",
    user = "test",
  )
