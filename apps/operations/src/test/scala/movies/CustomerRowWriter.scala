package es.eriktorr.lambda4s
package movies

import org.scalacheck.Gen

final class CustomerRowWriter {}

object CustomerRowWriter:
  val customerIdGen: Gen[Short] = Gen.choose[Short](0, Short.MaxValue)
