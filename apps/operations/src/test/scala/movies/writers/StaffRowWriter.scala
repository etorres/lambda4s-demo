package es.eriktorr.lambda4s
package movies.writers

import StringGenerators.stringBetween
import infrastructure.{MySqlTestTransactor, MySqlWriterSuite, RowWriter}
import movies.writers.StaffRowWriter.StaffRow

import cats.effect.IO
import org.scalacheck.{Arbitrary, Gen}

import java.time.LocalDateTime

final class StaffRowWriter(testTransactor: MySqlTestTransactor)
    extends MySqlWriterSuite[StaffRow](testTransactor)
    with RowWriter[StaffRow]:
  override def add(rows: List[StaffRow]): IO[Unit] = super.add(
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
              | password
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
              | '${row.password}'
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
      store_id: Byte,
      active: Boolean,
      username: String,
      password: String,
      last_update: Option[LocalDateTime],
  )

  val staffIdGen: Gen[Byte] = Gen.choose[Byte](0, Byte.MaxValue)

  def staffRowGen(
      addressIdGen: Gen[Short],
      staffIdGen: Gen[Byte],
      storeIdGen: Gen[Byte],
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
    None,
  )
