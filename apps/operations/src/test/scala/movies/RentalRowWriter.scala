package es.eriktorr.lambda4s
package movies

import org.scalacheck.Gen

final class RentalRowWriter {}

object RentalRowWriter:
  val rentalIdGen: Gen[Int] = Gen.choose(0, Int.MaxValue)
