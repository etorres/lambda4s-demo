package es.eriktorr.lambda4s
package infrastructure

import cats.effect.IO

sealed trait LocalStackProxy

object LocalStackProxy:
  final class LocalStackHttp4sProxy extends LocalStackProxy

  object LocalStackHttp4sProxy:

    import org.http4s.Uri.Scheme
    import org.http4s.client.Client
    import org.http4s.{Header, Uri}
    import org.typelevel.ci.CIStringSyntax

    def impl(client: Client[IO], bucket: String): Client[IO] = Client { request =>
      client.run(
        request
          .withUri(
            request.uri.copy(
              scheme = request.uri.scheme.map(_ => Scheme.http),
              authority = request.uri.authority.map(x =>
                x.copy(
                  host = Uri.RegName("localhost"),
                  port = Some(4566),
                ),
              ),
            ),
          )
          .withPathInfo(request.pathInfo.addSegment(Uri.Path.Segment(bucket)))
          .putHeaders(Header.Raw(ci"host", "localhost")),
      )
    }
  end LocalStackHttp4sProxy

  final class LocalStackSttpProxy extends LocalStackProxy

  object LocalStackSttpProxy:
    import sttp.capabilities.WebSockets
    import sttp.client3.{Identity, Request, Response, SttpBackend}
    import sttp.model.{Header, HeaderNames, Uri}
    import sttp.monad.MonadError

    def impl(backend: SttpBackend[IO, WebSockets], bucket: String): SttpBackend[IO, WebSockets] =
      new SttpBackend[IO, WebSockets]():
        override def send[T, R](request: Request[T, R]): IO[Response[T]] =
          backend.send(
            request
              .copy[Identity, T, R](
                headers = Header(HeaderNames.Host, "localhost") :: request.headers
                  .filterNot(_.is(HeaderNames.Host))
                  .toList,
                uri = request.uri.copy(
                  scheme = request.uri.scheme.map(_ => "http"),
                  authority = request.uri.authority.map(x =>
                    x.copy(
                      hostSegment = Uri.HostSegment("localhost"),
                      port = Some(4566),
                    ),
                  ),
                  pathSegments = request.uri.pathSegments.addSegment(Uri.PathSegment(bucket)),
                ),
              )
              .asInstanceOf[Request[T, Any]],
          )

        override def close(): IO[Unit] = backend.close()

        override def responseMonad: MonadError[IO] = backend.responseMonad
  end LocalStackSttpProxy
