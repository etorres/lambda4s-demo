package es.eriktorr.lambda4s
package refined.types

import munit.FunSuite

final class NonEmptyStringSuite extends FunSuite:
  test("should get some value from a non-empty string") {
    val obtained = NonEmptyString.from("Hello World!")
    val expected = Some(NonEmptyString.const("Hello World!"))
    assertEquals(obtained, expected)
  }

  test("should get none value from an empty string") {
    val obtained = NonEmptyString.from("")
    val expected = None
    assertEquals(obtained, expected)
  }

  test("should fail with an exception when the input value is empty") {
    interceptMessage[IllegalArgumentException]("Value cannot be empty") {
      NonEmptyString.unsafeFrom("")
    }
  }
