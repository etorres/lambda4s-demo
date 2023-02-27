package es.eriktorr.lambda4s
package movies

import infrastructure.{DatabaseTestConfiguration, MySqlWriterSuite}
import movies.CustomerRowWriter.customerIdGen
import movies.PaymentRowWriter.{formatter, PaymentRow}
import movies.RentalRowWriter.rentalIdGen
import movies.StaffRowWriter.staffIdGen

import cats.effect.IO
import org.scalacheck.Gen

import java.sql.Timestamp
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.concurrent.ExecutionContext

final class PaymentRowWriter(
    databaseTestConfiguration: DatabaseTestConfiguration,
    executionContext: ExecutionContext,
) extends MySqlWriterSuite[PaymentRow](databaseTestConfiguration, executionContext):
  override def add(rows: List[PaymentRow], rowToQuery: PaymentRow => String): IO[Unit] =
    super.add(
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
                | ${row.amount},
                | ${row.payment_date.format(formatter)},
                | ${row.last_update.toLocalDateTime.nn.format(formatter)}
                |)""".stripMargin,
    )

object PaymentRowWriter:
  private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

  final case class PaymentRow(
      payment_id: Short,
      customer_id: Short,
      staff_id: Byte,
      rental_id: Int,
      amount: Double,
      payment_date: LocalDateTime,
      last_update: Timestamp,
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
    amount <- Gen.choose(0.0d, 10000.0d)
    payment_date <- paymentDateGen
    last_update <- TemporalGenerators
      .after(payment_date.toLocalDate.nn)
      .map(localDate => Timestamp.valueOf(localDate.atStartOfDay).nn)
  yield PaymentRow(payment_id, customer_id, staff_id, rental_id, amount, payment_date, last_update)

/* TODO
CREATE TABLE payment (
  PRIMARY KEY  (payment_id),
  KEY idx_fk_staff_id (staff_id),
  KEY idx_fk_customer_id (customer_id),
  CONSTRAINT fk_payment_rental FOREIGN KEY (rental_id) REFERENCES rental (rental_id) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT fk_payment_customer FOREIGN KEY (customer_id) REFERENCES customer (customer_id) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_payment_staff FOREIGN KEY (staff_id) REFERENCES staff (staff_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
 */
