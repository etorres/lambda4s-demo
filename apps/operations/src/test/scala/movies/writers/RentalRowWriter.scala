package es.eriktorr.lambda4s
package movies.writers

import infrastructure.DatabaseTestConfiguration.mysqlDateTimeFormatter
import infrastructure.{MySqlTestTransactor, MySqlWriterSuite}
import movies.writers.RentalRowWriter.RentalRow

import cats.effect.IO
import org.scalacheck.Gen

import java.time.LocalDateTime

final class RentalRowWriter(testTransactor: MySqlTestTransactor)
    extends MySqlWriterSuite[RentalRow](testTransactor):
  def add(rows: List[RentalRow]): IO[Unit] = super.add(
    rows,
    row => s"""INSERT INTO rental (
              | rental_id,
              | rental_date,
              | inventory_id,
              | customer_id,
              | return_date,
              | staff_id
              |) VALUES (
              | ${row.rental_id},
              | '${row.rental_date.format(mysqlDateTimeFormatter)}',
              | ${row.inventory_id},
              | ${row.customer_id},
              | ${row.return_date.fold("NULL")(x => s"'${x.format(mysqlDateTimeFormatter)}'")},
              | ${row.staff_id}
              |)""".stripMargin,
  )

object RentalRowWriter:
  val rentalIdGen: Gen[Int] = Gen.choose(0, Int.MaxValue)

  final case class RentalRow(
      rental_id: Int,
      rental_date: LocalDateTime,
      inventory_id: Short,
      customer_id: Short,
      return_date: Option[LocalDateTime],
      staff_id: Byte,
      last_update: Option[LocalDateTime],
  )

  def rentalRowGen(
      customerIdGen: Gen[Short],
      inventoryIdGen: Gen[Short],
      rentalDateGen: Gen[LocalDateTime],
      rentalIdGen: Gen[Int],
      staffIdGen: Gen[Byte],
  ): Gen[RentalRow] = for
    rental_id <- rentalIdGen
    rental_date <- rentalDateGen
    inventory_id <- inventoryIdGen
    customer_id <- customerIdGen
    staff_id <- staffIdGen
  yield RentalRow(rental_id, rental_date, inventory_id, customer_id, None, staff_id, None)
