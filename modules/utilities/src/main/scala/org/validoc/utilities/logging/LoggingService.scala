package org.validoc.utilities.logging

import java.text.MessageFormat

import org.validoc.utilities.{ServiceTrees, ServiceType}
import org.validoc.utilities.kleisli.KleisliDelegate
import org.validoc.utilities.kleisli.Kleislis._
import utilities.kleisli.Kleisli

import scala.concurrent.ExecutionContext
import scala.reflect.ClassTag
import scala.util.{Failure, Success, Try}

trait LogData[T] extends (T => String)

object LogData {
  implicit def defaultSummaryLogData[T] = new LogData[T] {
    override def apply(v1: T) = v1.toString
  }
}

trait Logger {
  def info(t: String)

  def error(t: String, e: Throwable)
}

object Logger {
  def called[Req, Res](logger: Logger, pattern: String)(req: Req, res: Try[Res])(implicit logDataReq: LogData[Req], logDataRes: LogData[Res], logDataForThrowable: LogData[Throwable]) = res match {
    case Success(res) => logger.info(MessageFormat.format(pattern, logDataReq(req), logDataRes(res)))
    case Failure(e) => logger.error(MessageFormat.format(pattern, logDataReq(req), logDataForThrowable(e)), e)
  }

  implicit object DefaultPrintlnLogger extends Logger {
    override def info(t: String) = println(t)

    override def error(t: String, e: Throwable) = {
      println(t);
      e.printStackTrace()
    }
  }

}

case object LoggingService extends ServiceType
trait LoggingLanguage {

  def logging[Req: LogData : ClassTag, Res: LogData : ClassTag](pattern: String)
                                                               (implicit logger: Logger, ec: ExecutionContext, logDataForThrowable: LogData[Throwable], serviceTrees: ServiceTrees): KleisliDelegate[Req, Res] =
    new KleisliDelegate[Req, Res] {
      override def apply(delegate: Kleisli[Req, Res]) = serviceTrees.addOneChild(LoggingService, delegate.sideeffectWithReq(Logger.called(logger, pattern)), delegate)
    }
}
