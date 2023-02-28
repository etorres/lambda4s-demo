package es.eriktorr.lambda4s
package movies

import org.scalacheck.Gen

final class StoreRowWriter {}

object StoreRowWriter:
  final case class StoreRow(store_id: Short)

  val storeIdGen: Gen[Short] = Gen.choose[Short](0, Short.MaxValue)
