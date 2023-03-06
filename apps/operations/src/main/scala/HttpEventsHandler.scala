package es.eriktorr.lambda4s

import cats.effect.{IO, Resource}
import feral.lambda.events.{ApiGatewayProxyEventV2, ApiGatewayProxyStructuredResultV2}
import feral.lambda.http4s.ApiGatewayProxyHandler
import feral.lambda.{IOLambda, LambdaEnv}
import org.http4s.HttpRoutes
import org.http4s.client.Client
import org.http4s.ember.client.EmberClientBuilder
import org.typelevel.log4cats.Logger

object HttpEventsHandler
    extends IOLambda[ApiGatewayProxyEventV2, ApiGatewayProxyStructuredResultV2]:
  override def handler: Resource[IO, LambdaEnv[IO, ApiGatewayProxyEventV2] => IO[
    Option[ApiGatewayProxyStructuredResultV2],
  ]] = for
    configuration <- Resource.eval(HttpEventsConfiguration.load)
    httpClient <- EmberClientBuilder.default[IO].build
    logger <- Resource.eval(Log4sFactory.impl().create)
  yield implicit env => ApiGatewayProxyHandler(routesWith(configuration, httpClient)(using logger))

  private def routesWith(configuration: HttpEventsConfiguration, httpClient: Client[IO])(using
      logger: Logger[IO],
  ): HttpRoutes[IO] =
    import org.http4s.dsl.io.*
    HttpRoutes.of[IO] {
      case GET -> Root / "cumulative-revenue" => ???
      case GET -> Root / "exists-in-s3" => ???
      case GET -> Root / "films-by-rating" => ???
      case _ => NotFound()
    }
