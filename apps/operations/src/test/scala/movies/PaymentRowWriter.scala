package es.eriktorr.lambda4s
package movies

import infrastructure.{DatabaseTestConfiguration, MySqlWriterSuite}
import movies.PaymentRowWriter.PaymentRow

import cats.effect.IO

import java.sql.Timestamp
import java.time.LocalDateTime
import scala.concurrent.ExecutionContext

final class PaymentRowWriter(
    databaseTestConfiguration: DatabaseTestConfiguration,
    executionContext: ExecutionContext,
) extends MySqlWriterSuite[PaymentRow](databaseTestConfiguration, executionContext):
  override def add(rows: List[PaymentRow], rowToQuery: PaymentRow => String): IO[Unit] =
    super.add(
      rows,
      row => s"""INSERT INTO payment (
                |
                |) VALUES (
                |
                |)""".stripMargin,
    )

object PaymentRowWriter:
  final case class PaymentRow(
      payment_id: Short,
      customer_id: Short,
      staff_id: Byte,
      rental_id: Int,
      amount: Double,
      payment_date: LocalDateTime,
      last_update: Timestamp,
  )

/*
CREATE TABLE payment (
  PRIMARY KEY  (payment_id),
  KEY idx_fk_staff_id (staff_id),
  KEY idx_fk_customer_id (customer_id),
  CONSTRAINT fk_payment_rental FOREIGN KEY (rental_id) REFERENCES rental (rental_id) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT fk_payment_customer FOREIGN KEY (customer_id) REFERENCES customer (customer_id) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_payment_staff FOREIGN KEY (staff_id) REFERENCES staff (staff_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
 */
