package es.eriktorr.lambda4s
package movies

import infrastructure.{MySqlTestTransactor, MySqlWriterSuite}
import movies.StoreRowWriter.StoreRow

import cats.effect.IO
import org.scalacheck.Gen

import java.time.LocalDateTime

final class StoreRowWriter(testTransactor: MySqlTestTransactor)
    extends MySqlWriterSuite[StoreRow](testTransactor):
  def add(rows: List[StoreRow]): IO[Unit] = super.add(
    rows,
    row => s"""INSERT INTO store (
              | store_id,
              | manager_staff_id,
              | address_id
              |) VALUES (
              | ${row.store_id},
              | ${row.manager_staff_id},
              | ${row.address_id}
              |)
              |""".stripMargin,
  )

object StoreRowWriter:
  final case class StoreRow(
      store_id: Byte,
      manager_staff_id: Byte,
      address_id: Short,
      last_update: Option[LocalDateTime],
  )

  val storeIdGen: Gen[Byte] = Gen.choose[Byte](0, Byte.MaxValue)

  def storeRowGen(
      storeIdGen: Gen[Byte],
      managerStaffIdGen: Gen[Byte],
      addressIdGen: Gen[Short],
  ): Gen[StoreRow] = for
    store_id <- storeIdGen
    manager_staff_id <- managerStaffIdGen
    address_id <- addressIdGen
  yield StoreRow(store_id, manager_staff_id, address_id, None)
