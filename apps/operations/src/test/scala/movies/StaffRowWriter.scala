package es.eriktorr.lambda4s
package movies

import StringGenerators.stringBetween
import TemporalGenerators.localDateTimeGen
import infrastructure.DatabaseTestConfiguration.mysqlDateTimeFormatter
import infrastructure.{DatabaseTestConfiguration, MySqlWriterSuite}
import movies.AddressRowWriter.addressIdGen
import movies.StaffRowWriter.StaffRow
import movies.StoreRowWriter.storeIdGen

import cats.effect.IO
import com.fortysevendeg.scalacheck.datetime.jdk8.ArbitraryJdk8.arbLocalDateTimeJdk8
import org.scalacheck.{Arbitrary, Gen}

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext

final class StaffRowWriter(
    databaseTestConfiguration: DatabaseTestConfiguration,
    executionContext: ExecutionContext,
) extends MySqlWriterSuite[StaffRow](databaseTestConfiguration, executionContext):
  def add(rows: List[StaffRow]): IO[Unit] = super.add(
    rows,
    row => s"""INSERT INTO staff (
              | staff_id,
              | first_name,
              | last_name,
              | address_id,
              | picture,
              | email,
              | store_id,
              | active,
              | username,
              | password,
              | last_update
              |) VALUES (
              | ${row.staff_id},
              | '${row.first_name}',
              | '${row.last_name}',
              | ${row.address_id},
              | null,
              | '${row.email}',
              | ${row.store_id},
              | ${if row.active then 1 else 0},
              | '${row.username}',
              | '${row.password}',
              | '${row.last_update.format(mysqlDateTimeFormatter)}'
              |)
              |""".stripMargin,
  )

object StaffRowWriter:
  final case class StaffRow(
      staff_id: Byte,
      first_name: String,
      last_name: String,
      address_id: Short,
      picture: Option[String],
      email: String,
      store_id: Short,
      active: Boolean,
      username: String,
      password: String,
      last_update: LocalDateTime,
  )

  val staffIdGen: Gen[Byte] = Gen.choose[Byte](0, Byte.MaxValue)

  def staffRowGen(
      addressIdGen: Gen[Short] = addressIdGen,
      staffIdGen: Gen[Byte] = staffIdGen,
      storeIdGen: Gen[Short] = storeIdGen,
  ): Gen[StaffRow] = for
    staff_id <- staffIdGen
    first_name <- stringBetween(3, 45)
    last_name <- stringBetween(3, 45)
    address_id <- addressIdGen
    email <- stringBetween(10, 50)
    store_id <- storeIdGen
    active <- Arbitrary.arbBool.arbitrary
    username <- stringBetween(3, 16)
    password <- stringBetween(3, 40)
    last_update <- localDateTimeGen
  yield StaffRow(
    staff_id,
    first_name,
    last_name,
    address_id,
    None,
    email,
    store_id,
    active,
    username,
    password,
    last_update,
  )
