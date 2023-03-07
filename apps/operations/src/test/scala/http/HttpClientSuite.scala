package es.eriktorr.lambda4s
package http

import cats.effect.{IO, Resource}
import munit.CatsEffectSuite
import org.http4s.ember.client.EmberClientBuilder
import sttp.client3.*
import sttp.client3.impl.cats.FetchCatsBackend

final class HttpClientSuite extends CatsEffectSuite:
  test("should work with http4s".ignore) {
    EmberClientBuilder
      .default[IO]
      .build
      .use { httpClient =>
        httpClient.expect[String]("https://httpbin.org/get?hello=world")
      }
      .map(_.nonEmpty)
      .assert
  }

  test("should work with sttp") {
    Resource
      .make(IO.delay(FetchCatsBackend[IO]()))(_.close())
      .use(backend =>
        for
          response <- basicRequest
            .get(uri"https://httpbin.org/get?hello=world")
            .send(backend)
          body = response.body
        yield body,
      )
      .map(_.fold(_ => false, _.nonEmpty))
      .assert
  }
