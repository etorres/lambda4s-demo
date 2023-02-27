package es.eriktorr.lambda4s

import cats.effect.IO
import org.log4s.{Info, LogLevel}
import org.typelevel.log4cats.{LoggerFactory, LoggerName, SelfAwareStructuredLogger}

object Log4sFactory:
  def apply(logLevel: LogLevel): LoggerFactory[IO] = impl(logLevel)

  def impl(logLevel: LogLevel = Info): LoggerFactory[IO] = new LoggerFactory[IO]:
    override def create(implicit name: LoggerName): IO[SelfAwareStructuredLogger[IO]] =
      IO.delay(getLoggerFromName(name.value))

    override def getLoggerFromName(name: String): SelfAwareStructuredLogger[IO] =
      val _ = name
      Log4sLogger.impl(logLevel)

    override def fromName(name: String): IO[SelfAwareStructuredLogger[IO]] =
      IO.pure(getLoggerFromName(name))
