package es.eriktorr.lambda4s
package movies.writers

import StringGenerators.stringBetween
import TemporalGenerators.localDateTimeGen
import infrastructure.DatabaseTestConfiguration.mysqlDateTimeFormatter
import infrastructure.{MySqlTestTransactor, MySqlWriterSuite}
import movies.writers.CustomerRowWriter.CustomerRow

import cats.effect.IO
import org.scalacheck.{Arbitrary, Gen}

import java.time.LocalDateTime

final class CustomerRowWriter(testTransactor: MySqlTestTransactor)
    extends MySqlWriterSuite[CustomerRow](testTransactor):
  def add(rows: List[CustomerRow]): IO[Unit] = super.add(
    rows,
    row => s"""INSERT INTO customer (
              | customer_id,
              | store_id,
              | first_name,
              | last_name,
              | email,
              | address_id,
              | active,
              | create_date
              |) VALUES (
              | ${row.customer_id},
              | ${row.store_id},
              | '${row.first_name}',
              | '${row.last_name}',
              | '${row.email}',
              | ${row.address_id},
              | ${if row.active then 1 else 0},
              | '${row.create_date.format(mysqlDateTimeFormatter)}'
              |)""".stripMargin,
  )

object CustomerRowWriter:
  val customerIdGen: Gen[Short] = Gen.choose[Short](0, Short.MaxValue)

  final case class CustomerRow(
      customer_id: Short,
      store_id: Byte,
      first_name: String,
      last_name: String,
      email: String,
      address_id: Short,
      active: Boolean,
      create_date: LocalDateTime,
      last_update: Option[LocalDateTime],
  )

  def customerRowGen(
      addressIdGen: Gen[Short],
      customerIdGen: Gen[Short],
      storeIdGen: Gen[Byte],
  ): Gen[CustomerRow] = for
    customer_id <- customerIdGen
    store_id <- storeIdGen
    last_name <- stringBetween(3, 45)
    first_name <- stringBetween(3, 45)
    email <- stringBetween(10, 50)
    address_id <- addressIdGen
    active <- Arbitrary.arbBool.arbitrary
    create_date <- localDateTimeGen
  yield CustomerRow(
    customer_id,
    store_id,
    first_name,
    last_name,
    email,
    address_id,
    active,
    create_date,
    None,
  )
