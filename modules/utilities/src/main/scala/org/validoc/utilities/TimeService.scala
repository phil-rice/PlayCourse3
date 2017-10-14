package org.validoc.utilities

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

trait NanoTimeService {
  def apply(): Long

  def apply[Req, Res](delegate: Req => Future[Res])(sideeffect: (Try[Res], Long) => Unit)(implicit ex: ExecutionContext): Req => Future[Res] = { req =>
    val startTime = apply()
    val result = delegate(req)
    result.onComplete(res => sideeffect(res, apply() - startTime))
    result
  }
}

object NanoTimeService {
  implicit val default = SystemClockNanoTimeService
}

object SystemClockNanoTimeService extends NanoTimeService {
  override def apply(): Long = System.nanoTime()
}



