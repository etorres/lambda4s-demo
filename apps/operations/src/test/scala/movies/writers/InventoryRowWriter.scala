package es.eriktorr.lambda4s
package movies.writers

import infrastructure.{MySqlTestTransactor, MySqlWriterSuite, RowWriter}
import movies.writers.InventoryRowWriter.InventoryRow

import cats.effect.IO
import org.scalacheck.Gen

import java.time.LocalDateTime

final class InventoryRowWriter(testTransactor: MySqlTestTransactor)
    extends MySqlWriterSuite[InventoryRow](testTransactor)
    with RowWriter[InventoryRow]:
  override def add(rows: List[InventoryRow]): IO[Unit] = super.add(
    rows,
    row => s"""INSERT INTO inventory (
              | inventory_id,
              | film_id,
              | store_id
              |) VALUES (
              | ${row.inventory_id},
              | ${row.film_id},
              | ${row.store_id}
              |)""".stripMargin,
  )

object InventoryRowWriter:
  final case class InventoryRow(
      inventory_id: Short,
      film_id: Short,
      store_id: Byte,
      last_update: Option[LocalDateTime],
  )

  val inventoryIdGen: Gen[Short] = Gen.choose[Short](0, Short.MaxValue)

  def inventoryRowGen(
      filmIdGen: Gen[Short],
      inventoryIdGen: Gen[Short],
      storeIdGen: Gen[Byte],
  ): Gen[InventoryRow] = for
    inventory_id <- inventoryIdGen
    film_id <- filmIdGen
    store_id <- storeIdGen
  yield InventoryRow(inventory_id, film_id, store_id, None)
