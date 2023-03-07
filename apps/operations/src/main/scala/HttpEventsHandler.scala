package es.eriktorr.lambda4s

import aws.S3Objects
import database.DatabaseConfiguration
import database.mysql.MySqlTransactor
import movies.MoviesReader.{CumulativeRevenue, RatingCounter}
import movies.{MoviesReader, Rating}

import cats.effect.{IO, Resource}
import feral.lambda.events.{ApiGatewayProxyEventV2, ApiGatewayProxyStructuredResultV2}
import feral.lambda.http4s.ApiGatewayProxyHandler
import feral.lambda.{IOLambda, LambdaEnv}
import io.circe.syntax.EncoderOps
import io.circe.{Encoder, Json}
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.client.Client
import org.http4s.ember.client.EmberClientBuilder
import org.typelevel.log4cats.Logger

import java.time.LocalDateTime

object HttpEventsHandler
    extends IOLambda[ApiGatewayProxyEventV2, ApiGatewayProxyStructuredResultV2]:
  override def handler: Resource[IO, LambdaEnv[IO, ApiGatewayProxyEventV2] => IO[
    Option[ApiGatewayProxyStructuredResultV2],
  ]] = for
    configuration <- Resource.eval(HttpEventsConfiguration.load)
    httpClient <- EmberClientBuilder.default[IO].build
    logger <- Resource.eval(Log4sFactory.impl().create)
  yield implicit env => ApiGatewayProxyHandler(routesWith(configuration, httpClient)(using logger))

  private object DateTimeRangeVar:
    def unapply(str: String): Option[DateRange[LocalDateTime]] =
      val fromDate = LocalDateTime.now().nn
      str match
        case "last-month" => Some(DateRange(fromDate, fromDate.plusMonths(-1L).nn))
        case "last-week" => Some(DateRange(fromDate, fromDate.plusWeeks(-1L).nn))
        case _ => None

  given Encoder[CumulativeRevenue] = (value: CumulativeRevenue) =>
    Json.obj(
      ("paymentDate", value.paymentDate.asJson),
      ("amount", value.amount.asJson),
      ("cumulativeRevenue", value.cumulativeRevenue.asJson),
    )

  given Encoder[Rating] = Encoder.encodeString.contramap[Rating](_.toString)

  given Encoder[RatingCounter] = (value: RatingCounter) =>
    Json.obj(("rating", value.rating.asJson), ("count", value.count.asJson))

  private def routesWith(configuration: HttpEventsConfiguration, httpClient: Client[IO])(using
      logger: Logger[IO],
  ): HttpRoutes[IO] =
    import org.http4s.dsl.io.*
    HttpRoutes.of[IO] {
      case GET -> Root / "cumulative-revenue" / DateTimeRangeVar(dateTimeRange) =>
        queryWith[List[CumulativeRevenue]](
          configuration.databaseConfiguration,
          moviesReader => moviesReader.cumulativeRevenueDuring(dateTimeRange),
        ).flatMap(cumulativeRevenue => Ok(cumulativeRevenue.asJson))
      case GET -> Root / "exists-in-s3" / objectKey =>
        val s3Objects = S3Objects.impl(
          configuration.awsConfiguration,
          httpClient,
        )
        s3Objects.exists(configuration.s3Bucket, objectKey).flatMap(exists => Ok(exists.asJson))
      case GET -> Root / "films-by-rating" =>
        queryWith[List[RatingCounter]](
          configuration.databaseConfiguration,
          moviesReader => moviesReader.filmsByRating,
        ).flatMap(filmsByRating => Ok(filmsByRating.asJson))
      case _ => NotFound()
    }

  private def queryWith[A](
      databaseConfiguration: DatabaseConfiguration,
      query: MoviesReader => IO[A],
  )(using
      logger: Logger[IO],
  ) = for
    executionContext <- IO.executionContext
    response <- MySqlTransactor
      .of(databaseConfiguration, executionContext)
      .use { transactor =>
        val moviesReader = MoviesReader.impl(transactor)
        query(moviesReader)
      }
  yield response
