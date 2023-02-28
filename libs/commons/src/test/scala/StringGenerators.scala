package es.eriktorr.lambda4s

import org.scalacheck.Gen

object StringGenerators:
  def stringOfLength(length: Int, charGen: Gen[Char]): Gen[String] =
    Gen.listOfN(length, charGen).map(_.mkString)

  def stringBetween(minLength: Int, maxLength: Int): Gen[String] = for
    length <- Gen.choose(minLength, maxLength)
    str <- stringOfLength(length, Gen.alphaNumChar)
  yield str

  val nonEmptyStringGen: Gen[String] = stringBetween(5, 20)
