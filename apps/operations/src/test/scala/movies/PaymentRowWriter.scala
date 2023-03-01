package es.eriktorr.lambda4s
package movies

import infrastructure.DatabaseTestConfiguration.mysqlDateTimeFormatter
import infrastructure.{MySqlTestTransactor, MySqlWriterSuite}
import movies.PaymentRowWriter.PaymentRow

import cats.effect.IO
import org.scalacheck.Gen

import java.time.LocalDateTime

final class PaymentRowWriter(testTransactor: MySqlTestTransactor)
    extends MySqlWriterSuite[PaymentRow](testTransactor):
  def add(rows: List[PaymentRow]): IO[Unit] = super.add(
    rows,
    row => s"""INSERT INTO payment (
              | payment_id,
              | customer_id,
              | staff_id,
              | rental_id,
              | amount,
              | payment_date
              |) VALUES (
              | ${row.payment_id},
              | ${row.customer_id},
              | ${row.staff_id},
              | ${row.rental_id},
              | '${row.amount}',
              | '${row.payment_date.format(mysqlDateTimeFormatter)}'
              |)""".stripMargin.replaceAll("\\R", "").nn,
  )

object PaymentRowWriter:
  final case class PaymentRow(
      payment_id: Short,
      customer_id: Short,
      staff_id: Byte,
      rental_id: Int,
      amount: Double,
      payment_date: LocalDateTime,
      last_update: Option[LocalDateTime],
  )

  val paymentIdGen: Gen[Short] = Gen.choose[Short](0, Short.MaxValue)

  def paymentRowGen(
      customerIdGen: Gen[Short],
      paymentDateGen: Gen[LocalDateTime],
      paymentIdGen: Gen[Short],
      rentalIdGen: Gen[Int],
      staffIdGen: Gen[Byte],
  ): Gen[PaymentRow] = for
    payment_id <- paymentIdGen
    customer_id <- customerIdGen
    staff_id <- staffIdGen
    rental_id <- rentalIdGen
    amount <- Gen.choose(0.0d, 100.0d).map(x => math.floor(x * 100.0d) / 100.0d)
    payment_date <- paymentDateGen
  yield PaymentRow(payment_id, customer_id, staff_id, rental_id, amount, payment_date, None)
