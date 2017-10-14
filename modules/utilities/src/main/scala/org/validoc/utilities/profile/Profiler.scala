package org.validoc.utilities.profile

import org.validoc.utilities.NanoTimeService
import org.validoc.utilities.kleisli.KleisliDelegate
import utilities.kleisli.Kleisli

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class TryProfileData {
  def clearData = {
    succeededData.clearData
    failedData.clearData
  }

  val succeededData = new ProfileData
  val failedData = new ProfileData

  def event(result: Try[_], nanos: Long): Unit = result match {
    case Success(_) => succeededData.event(nanos)
    case Failure(_) => failedData.event(nanos)
  }
}


class ProfilingKleisli[Req, Res](tryProfileData: TryProfileData, delegate: Kleisli[Req, Res])(implicit timeService: NanoTimeService, ex: ExecutionContext) extends Kleisli[Req, Res] {

  val wrappedFn = timeService(delegate)(tryProfileData.event)

  override def apply(request: Req): Future[Res] = wrappedFn(request)
}

trait ProfilingLanguage {

  def profile[Req, Res](tryData: TryProfileData)(implicit  ex: ExecutionContext) = new KleisliDelegate[Req, Res] {
    override def apply(delegate: Kleisli[Req, Res]) = new ProfilingKleisli[Req, Res](tryData, delegate)
  }

}