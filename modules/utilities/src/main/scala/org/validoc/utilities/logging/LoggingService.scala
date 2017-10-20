package org.validoc.utilities.logging

import java.text.MessageFormat

import org.validoc.utilities.kleisli.KleisliDelegate

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success, Try}
import org.validoc.utilities.kleisli.Kleislis._
import utilities.kleisli.Kleisli

trait LogData[T] extends (T => String)

object LogData {
  implicit def defaultSummaryLogData[T] = new LogData[T] {
    override def apply(v1: T) = v1.toString
  }
}


trait Logger {
  def called[Req, Res](pattern: String)(req: Req, res: Try[Res])(implicit logDataReq: LogData[Req], logDataRes: LogData[Res], logDataForThrowable: LogData[Throwable]) = res match {
    case Success(res) => info(MessageFormat.format(pattern, logDataReq(req), logDataRes(res)))
    case Failure(e) => error(MessageFormat.format(pattern, logDataReq(req), logDataForThrowable(e)), e)
  }

  def info(t: String)

  def error(t: String, e: Throwable)
}

object Logger {

  implicit object DefaultPrintlnLogger extends Logger {
    override def info(t: String) = println(t)

    override def error(t: String, e: Throwable) = {
      println(t);
      e.printStackTrace()
    }
  }

}

trait LoggingLanguage {

  import org.validoc.utilities.Arrows._

  def logging[Req: LogData, Res: LogData](pattern: String)
                                         (implicit logger: Logger, ec: ExecutionContext, logDataForThrowable: LogData[Throwable]): KleisliDelegate[Req, Res] =
    new KleisliDelegate[Req, Res] {
      override def apply(delegate: Kleisli[Req, Res]) = delegate.sideeffectWithReq(logger.called(pattern))
    }
}
