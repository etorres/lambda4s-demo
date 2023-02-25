package es.eriktorr.lambda4s
package database

import cats.effect.IO
import typings.std.PromiseLike

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration
import scala.util.Success

object PromiseLikeSyntax:
  extension [A](self: PromiseLike[A])
    def toIO(using executionContext: ExecutionContext): IO[A] = toIO(timeout = None)

    def toIO(timeout: FiniteDuration)(using executionContext: ExecutionContext): IO[A] = toIO(
      Some(timeout),
    )

    private def toIO(timeout: Option[FiniteDuration])(using
        executionContext: ExecutionContext,
    ): IO[A] =
      val blockingIO = IO.blocking(self.toFuture.andThen { case Success(result) => result })
      IO.fromFuture(timeout.fold(blockingIO)(blockingIO.timeout))
