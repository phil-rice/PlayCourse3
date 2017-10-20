package org.validoc.utilities.debugEndpoint

import org.validoc.utilities.{ServiceTrees, ServiceType}
import org.validoc.utilities.kleisli.KleisliDelegate
import utilities.kleisli.Kleisli

import scala.concurrent.ExecutionContext

trait DebugEndPoint extends ServiceType

trait MakeDebugQuery[Req] extends (String => Req)

trait DebugToString[Res] extends (Res => String)

object DebugToString {
  implicit def default[T] = new DebugToString[T] {
    override def apply(v1: T) = v1.toString
  }
}


trait DebugEndPointLanguage {

  import org.validoc.utilities.Arrows._
  import org.validoc.utilities.kleisli.Kleislis._

  var allDebugEndPoints = Map[String, Kleisli[String, String]]()

  def debug[Req, Res](name: String)(implicit makeQuery: MakeDebugQuery[Req], debugToString: DebugToString[Res], ex: ExecutionContext, serviceTrees: ServiceTrees): KleisliDelegate[Req, Res] =
    new KleisliDelegate[Req, Res] {
      override def apply(kleisli: Kleisli[Req, Res]): Kleisli[Req, Res] = {
        val debugEndPoint: Kleisli[String, String] = makeQuery ~> kleisli ~> debugToString
        allDebugEndPoints = allDebugEndPoints + (name -> debugEndPoint)
        serviceTrees.add[DebugEndPoint].addOneChild(debugEndPoint, kleisli)
        kleisli
      }
    }
}
