package es.eriktorr.lambda4s
package movies

import org.scalacheck.Gen

final class StaffRowWriter {}

object StaffRowWriter:
  val staffIdGen: Gen[Byte] = Gen.choose[Byte](0, Byte.MaxValue)
