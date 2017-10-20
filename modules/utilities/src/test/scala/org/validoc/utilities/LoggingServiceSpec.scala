package org.validoc.utilities

import org.validoc.utilities.kleisli.KleisliDelegate
import org.validoc.utilities.logging.{LogData, Logger}
import utilities.kleisli.Kleisli

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits._

class LoggingServiceSpec extends UnitSpec {

  behavior of "The Logging Service"

  import org.validoc.utilities.kleisli.Kleislis._

  trait LogRequestForTest

  trait LogResponseForTest

  case class LoggingServiceContext(delegate: Kleisli[LogRequestForTest, LogResponseForTest], request: LogRequestForTest, response: LogResponseForTest)
                                  (implicit val logger: Logger, val logDataForRequest: LogData[LogRequestForTest], val logDataForResponse: LogData[LogResponseForTest], logDataForThrowable: LogData[Throwable]) {
    val loggingService = new KleisliPimper(delegate) |+| logging[LogRequestForTest, LogResponseForTest]("pattern [{0}] [{1}]")
  }

  def setup(fn: (LoggingServiceContext) => Unit): Unit = {
    val req = stub[LogRequestForTest]
    val res = stub[LogResponseForTest]
    val kleisli = stub[LogRequestForTest => Future[LogResponseForTest]]
    val logRequestForTest = stub[LogData[LogRequestForTest]]
    val logResponseForTest = stub[LogData[LogResponseForTest]]
    val logResponseForThrowable = stub[LogData[Throwable]]
    val logger = stub[Logger]
    fn(LoggingServiceContext(kleisli, req, res)(logger, logRequestForTest, logResponseForTest, logResponseForThrowable))
  }

  it should "return the result of the delegate when successful" in {
    setup {
      context =>
        import context._
        (delegate.apply _) when request returns Future.successful(response)
        loggingService(request).await shouldBe response
    }
  }
  it should "return the result of the delegate when exception" in {
    setup {
      context =>
        import context._
        val exception = new RuntimeException
        (delegate.apply _) when request returns Future.failed(exception)
        intercept[RuntimeException](loggingService(request).await) shouldBe exception
    }
  }
}

