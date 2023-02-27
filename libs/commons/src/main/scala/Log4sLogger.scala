package es.eriktorr.lambda4s

import cats.effect.IO
import org.log4s.*
import org.typelevel.log4cats.SelfAwareStructuredLogger

object Log4sLogger:
  private val logger = getLogger

  def apply(logLevel: LogLevel): SelfAwareStructuredLogger[IO] = impl(logLevel)

  def impl(logLevel: LogLevel): SelfAwareStructuredLogger[IO] = new SelfAwareStructuredLogger[IO]:
    @inline override def isTraceEnabled: IO[Boolean] = IO.pure(logLevel == Trace)

    @inline override def isDebugEnabled: IO[Boolean] = IO.pure(logLevel match
      case Debug | Trace => true
      case _ => false,
    )

    @inline override def isInfoEnabled: IO[Boolean] = IO.pure(logLevel match
      case Debug | Trace | Info => true
      case _ => false,
    )

    @inline override def isWarnEnabled: IO[Boolean] = IO.pure(logLevel match
      case Debug | Trace | Info | Warn => true
      case _ => false,
    )

    @inline override def isErrorEnabled: IO[Boolean] = IO.pure(true)

    override def trace(ctx: Map[String, String])(msg: => String): IO[Unit] = isTraceEnabled.ifM(
      ifTrue = IO.blocking(MDC.withCtx(ctx.toList*)(logger.trace(msg))),
      ifFalse = IO.unit,
    )

    override def trace(ctx: Map[String, String], t: Throwable)(msg: => String): IO[Unit] =
      isTraceEnabled.ifM(
        ifTrue = IO.blocking(MDC.withCtx(ctx.toList*)(logger.trace(t)(msg))),
        ifFalse = IO.unit,
      )

    override def trace(message: => String): IO[Unit] =
      isTraceEnabled.ifM(ifTrue = IO.blocking(logger.trace(message)), ifFalse = IO.unit)

    override def trace(t: Throwable)(message: => String): IO[Unit] =
      isTraceEnabled.ifM(ifTrue = IO.blocking(logger.trace(t)(message)), ifFalse = IO.unit)

    override def debug(ctx: Map[String, String])(msg: => String): IO[Unit] = isDebugEnabled.ifM(
      ifTrue = IO.blocking(MDC.withCtx(ctx.toList*)(logger.debug(msg))),
      ifFalse = IO.unit,
    )

    override def debug(ctx: Map[String, String], t: Throwable)(msg: => String): IO[Unit] =
      isDebugEnabled.ifM(
        ifTrue = IO.blocking(MDC.withCtx(ctx.toList*)(logger.debug(t)(msg))),
        ifFalse = IO.unit,
      )

    override def debug(message: => String): IO[Unit] =
      isDebugEnabled.ifM(ifTrue = IO.blocking(logger.debug(message)), ifFalse = IO.unit)

    override def debug(t: Throwable)(message: => String): IO[Unit] =
      isDebugEnabled.ifM(ifTrue = IO.blocking(logger.debug(t)(message)), ifFalse = IO.unit)

    override def info(ctx: Map[String, String])(msg: => String): IO[Unit] = isInfoEnabled.ifM(
      ifTrue = IO.blocking(MDC.withCtx(ctx.toList*)(logger.info(msg))),
      ifFalse = IO.unit,
    )

    override def info(ctx: Map[String, String], t: Throwable)(msg: => String): IO[Unit] =
      isInfoEnabled.ifM(
        ifTrue = IO.blocking(MDC.withCtx(ctx.toList*)(logger.info(t)(msg))),
        ifFalse = IO.unit,
      )

    override def info(message: => String): IO[Unit] =
      isInfoEnabled.ifM(ifTrue = IO.blocking(logger.info(message)), ifFalse = IO.unit)

    override def info(t: Throwable)(message: => String): IO[Unit] =
      isInfoEnabled.ifM(ifTrue = IO.blocking(logger.info(t)(message)), ifFalse = IO.unit)

    override def warn(ctx: Map[String, String])(msg: => String): IO[Unit] = isWarnEnabled.ifM(
      ifTrue = IO.blocking(MDC.withCtx(ctx.toList*)(logger.warn(msg))),
      ifFalse = IO.unit,
    )

    override def warn(ctx: Map[String, String], t: Throwable)(msg: => String): IO[Unit] =
      isWarnEnabled.ifM(
        ifTrue = IO.blocking(MDC.withCtx(ctx.toList*)(logger.warn(t)(msg))),
        ifFalse = IO.unit,
      )

    override def warn(message: => String): IO[Unit] =
      isWarnEnabled.ifM(ifTrue = IO.blocking(logger.warn(message)), ifFalse = IO.unit)

    override def warn(t: Throwable)(message: => String): IO[Unit] =
      isWarnEnabled.ifM(ifTrue = IO.blocking(logger.warn(t)(message)), ifFalse = IO.unit)

    override def error(ctx: Map[String, String])(msg: => String): IO[Unit] = isErrorEnabled.ifM(
      ifTrue = IO.blocking(MDC.withCtx(ctx.toList*)(logger.error(msg))),
      ifFalse = IO.unit,
    )

    override def error(ctx: Map[String, String], t: Throwable)(msg: => String): IO[Unit] =
      isErrorEnabled.ifM(
        ifTrue = IO.blocking(MDC.withCtx(ctx.toList*)(logger.error(t)(msg))),
        ifFalse = IO.unit,
      )

    override def error(message: => String): IO[Unit] =
      isErrorEnabled.ifM(ifTrue = IO.blocking(logger.error(message)), ifFalse = IO.unit)

    override def error(t: Throwable)(message: => String): IO[Unit] =
      isErrorEnabled.ifM(ifTrue = IO.blocking(logger.error(t)(message)), ifFalse = IO.unit)
