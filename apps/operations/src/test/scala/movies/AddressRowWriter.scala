package es.eriktorr.lambda4s
package movies

import StringGenerators.stringBetween
import infrastructure.{MySqlTestTransactor, MySqlWriterSuite}
import movies.AddressRowWriter.AddressRow

import cats.effect.IO
import org.scalacheck.Gen

import java.time.LocalDateTime

final class AddressRowWriter(testTransactor: MySqlTestTransactor)
    extends MySqlWriterSuite[AddressRow](testTransactor):
  def add(rows: List[AddressRow]): IO[Unit] = super.add(
    rows,
    row => s"""INSERT INTO address (
              | address_id,
              | address,
              | address2,
              | district,
              | city_id,
              | postal_code,
              | phone,
              | location
              |) VALUES (
              | ${row.address_id},
              | '${row.address}',
              | '${row.address2}',
              | '${row.district}',
              | ${row.city_id},
              | '${row.postal_code}',
              | '${row.phone}',
              | ${row.location}
              |)""".stripMargin,
  )

object AddressRowWriter:
  final case class AddressRow(
      address_id: Short,
      address: String,
      address2: String,
      district: String,
      city_id: Short,
      postal_code: String,
      phone: String,
      location: String,
      last_update: Option[LocalDateTime],
  )

  val addressIdGen: Gen[Short] = Gen.choose[Short](0, Short.MaxValue)

  private val locationGen: Gen[String] = Gen.oneOf(
    "/*!50705 0x0000000001010000003E0A325D63345CC0761FDB8D99D94840*/",
    "/*!50705 0x0000000001010000008E10D4DF812463404EE08C5022A23BC0*/",
    "/*!50705 0x000000000101000000CDC4196863345CC01DEE7E7099D94840*/",
    "/*!50705 0x0000000001010000005B0DE4341F26634042D6AE6422A23BC0*/",
    "/*!50705 0x00000000010100000028D1370E21376040ABB58BC45F944040*/",
  )

  def addressRowGen(addressIdGen: Gen[Short], cityIdGen: Gen[Short]): Gen[AddressRow] = for
    address_id <- addressIdGen
    address <- stringBetween(3, 50)
    address2 <- stringBetween(3, 50)
    district <- stringBetween(3, 20)
    city_id <- cityIdGen
    postal_code <- stringBetween(3, 10)
    phone <- stringBetween(3, 20)
    location <- locationGen
  yield AddressRow(
    address_id,
    address,
    address2,
    district,
    city_id,
    postal_code,
    phone,
    location,
    None,
  )
