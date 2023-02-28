package es.eriktorr.lambda4s
package movies

import infrastructure.{DatabaseTestConfiguration, MySqlWriterSuite}
import movies.CustomerRowWriter.customerIdGen
import movies.PaymentRowWriter.{mysqlDateTimeFormatter, PaymentRow}
import movies.RentalRowWriter.rentalIdGen
import movies.StaffRowWriter.staffIdGen

import cats.effect.IO
import org.scalacheck.Gen

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.concurrent.ExecutionContext

final class PaymentRowWriter(
    databaseTestConfiguration: DatabaseTestConfiguration,
    executionContext: ExecutionContext,
) extends MySqlWriterSuite[PaymentRow](databaseTestConfiguration, executionContext):
  def add(rows: List[PaymentRow]): IO[Unit] = super.add(
    rows,
    row => s"""INSERT INTO payment (
              | payment_id,
              | customer_id,
              | staff_id,
              | rental_id,
              | amount,
              | payment_date,
              | last_update
              |) VALUES (
              | ${row.payment_id},
              | ${row.customer_id},
              | ${row.staff_id},
              | ${row.rental_id},
              | '${row.amount}',
              | '${row.payment_date.format(mysqlDateTimeFormatter)}',
              | '${row.last_update.format(mysqlDateTimeFormatter)}'
              |)""".stripMargin,
  )

object PaymentRowWriter:
  private val mysqlDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

  final case class PaymentRow(
      payment_id: Short,
      customer_id: Short,
      staff_id: Byte,
      rental_id: Int,
      amount: Double,
      payment_date: LocalDateTime,
      last_update: LocalDateTime,
  )

  val paymentIdGen: Gen[Short] = Gen.choose[Short](0, Short.MaxValue)

  def paymentRowGen(
      paymentIdGen: Gen[Short] = paymentIdGen,
      customerIdGen: Gen[Short] = customerIdGen,
      staffIdGen: Gen[Byte] = staffIdGen,
      rentalIdGen: Gen[Int] = rentalIdGen,
      paymentDateGen: Gen[LocalDateTime],
  ): Gen[PaymentRow] = for
    payment_id <- paymentIdGen
    customer_id <- customerIdGen
    staff_id <- staffIdGen
    rental_id <- rentalIdGen
    amount <- Gen.choose(0.0d, 100.0d).map(x => math.floor(x * 100.0d) / 100.0d)
    payment_date <- paymentDateGen
    last_update <- TemporalGenerators.after(payment_date)
  yield PaymentRow(payment_id, customer_id, staff_id, rental_id, amount, payment_date, last_update)
