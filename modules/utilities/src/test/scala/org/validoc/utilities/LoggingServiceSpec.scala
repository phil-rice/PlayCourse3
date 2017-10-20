package org.validoc.utilities

import org.validoc.utilities.logging.{LogData, Logger}

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future

trait LogRequestForTest

trait LogResponseForTest

class LoggingServiceSpec extends UnitSpec {

  behavior of "The Logging Service"

  import org.validoc.utilities.kleisli.Kleislis._

  class LoggingServiceContext {
    val request = stub[LogRequestForTest]
    val response = stub[LogResponseForTest]
    val exception = new RuntimeException
    implicit val logRequestForTest = stub[LogData[LogRequestForTest]]
    implicit val logResponseForTest = stub[LogData[LogResponseForTest]]
    implicit val logResponseForThrowable = stub[LogData[Throwable]]
    (logRequestForTest.apply _) when request returns "RequestLD"
    (logResponseForTest.apply _) when response returns "ResponseLD"
    (logResponseForThrowable.apply _) when exception returns "ExceptionLD"
    implicit val logger = stub[Logger]

    val delegate = stub[LogRequestForTest => Future[LogResponseForTest]]
    val somePattern = "pattern [{0}] [{1}]"
    val loggingService = new KleisliPimper(delegate) |+| logging[LogRequestForTest, LogResponseForTest](somePattern)
  }

  it should "return the result of the delegate when successful" in {
    val context = new LoggingServiceContext
    import context._
    (delegate.apply _) when request returns Future.successful(response)
    loggingService(request).await shouldBe response
  }

  it should "return the result of the delegate when exception" in {
    val context = new LoggingServiceContext
    import context._
    (delegate.apply _) when request returns Future.failed(exception)
    intercept[RuntimeException](loggingService(request).await) shouldBe exception
  }

  it should "Send the request and response to the logger when succeeded" in {
    val context = new LoggingServiceContext
    import context._
    (delegate.apply _) when request returns Future.successful(response)
    loggingService(request).await
    (logger.info _).verify("pattern [RequestLD] [ResponseLD]")
  }
  it should "Send the request and error to the logger when failed" in {
    val context = new LoggingServiceContext
    import context._
    (delegate.apply _) when request returns Future.failed(exception)
    intercept[RuntimeException](loggingService(request).await)
    (logger.error _).verify("pattern [RequestLD] [ExceptionLD]", exception)

  }
}

