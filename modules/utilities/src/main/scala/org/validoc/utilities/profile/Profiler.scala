package org.validoc.utilities.profile

import org.validoc.utilities.{DisplayString, NanoTimeService, ServiceTrees, ServiceType}
import org.validoc.utilities.kleisli.KleisliDelegate
import utilities.kleisli.Kleisli

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag
import scala.util.{Failure, Success, Try}

object TryProfileData {

  implicit object DisplayStringForTryProfileData extends DisplayString[TryProfileData] {
    override def apply(pd: TryProfileData) = "Profiling"
  }

}

class TryProfileData extends ServiceType {
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

  def profile[Req: ClassTag, Res: ClassTag](tryData: TryProfileData)(implicit serviceTrees: ServiceTrees, ex: ExecutionContext) = new KleisliDelegate[Req, Res] {
    override def apply(delegate: Kleisli[Req, Res]) = serviceTrees.add(tryData).addOneChild[Req, Res](new ProfilingKleisli[Req, Res](tryData, delegate), delegate)
  }

}