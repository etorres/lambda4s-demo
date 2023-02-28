package es.eriktorr.lambda4s
package movies

import StringGenerators.stringBetween
import TemporalGenerators.localDateTimeGen
import movies.CityRowWriter.cityIdGen

import com.fortysevendeg.scalacheck.datetime.jdk8.ArbitraryJdk8.arbLocalDateTimeJdk8
import org.scalacheck.Gen

import java.time.LocalDateTime

final class AddressRowWriter {}

object AddressRowWriter:
  final case class AddressRow(
      address_id: Short,
      address: String,
      address2: String,
      district: String,
      city_id: Short,
      postal_code: String,
      phone: String,
      last_update: LocalDateTime,
  )

  val addressIdGen: Gen[Short] = Gen.choose[Short](0, Short.MaxValue)

  def addressRowGen(
      addressIdGen: Gen[Short] = addressIdGen,
      cityIdGen: Gen[Short] = cityIdGen,
  ): Gen[AddressRow] = for
    address_id <- addressIdGen
    address <- stringBetween(3, 50)
    address2 <- stringBetween(3, 50)
    district <- stringBetween(3, 20)
    city_id <- cityIdGen
    postal_code <- stringBetween(3, 10)
    phone <- stringBetween(3, 20)
    last_update <- localDateTimeGen
  yield AddressRow(
    address_id,
    address,
    address2,
    district,
    city_id,
    postal_code,
    phone,
    last_update,
  )
