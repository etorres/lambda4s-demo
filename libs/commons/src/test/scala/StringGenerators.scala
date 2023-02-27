package es.eriktorr.lambda4s

import org.scalacheck.Gen

object StringGenerators:
  def stringOfLength(length: Int, charGen: Gen[Char]): Gen[String] =
    Gen.listOfN(length, charGen).map(_.mkString)

  val nonEmptyStringGen: Gen[String] = for
    len <- Gen.choose(5, 20)
    str <- stringOfLength(len, Gen.alphaNumChar)
  yield str
