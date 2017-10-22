package org.validoc.utilities.endpoint

import org.validoc.utilities.Arrows._
import org.validoc.utilities.{ServiceTrees, ServiceType}
import org.validoc.utilities.kleisli.KleisliDelegate
import org.validoc.utilities.kleisli.Kleislis._
import utilities.kleisli.Kleisli

import scala.concurrent.ExecutionContext
import scala.reflect.ClassTag

case class EndPoint(name: String) extends ServiceType

trait MakeReqFromHttpReq[HttpReq, Req] extends (HttpReq => Req)

trait EndPointToRes[HttpRes, T] extends (T => HttpRes)

trait EndPointLanguage[HttpReq, HttpRes] {


  def endPoint[Req, Res](name: String)(implicit makeQuery: MakeReqFromHttpReq[HttpReq, Req], endpointToHttpRes: EndPointToRes[HttpRes, Res], ex: ExecutionContext, serviceTrees: ServiceTrees, httpReqCT: ClassTag[HttpReq], httpResCT: ClassTag[HttpRes]): KleisliDelegate[Req, Res] =
    new KleisliDelegate[Req, Res] {
      override def apply(kleisli: Kleisli[Req, Res]): Kleisli[Req, Res] = {
        val endPoint: Kleisli[HttpReq, HttpRes] = makeQuery ~> kleisli ~> endpointToHttpRes
        serviceTrees.add(EndPoint(name)).addOneChild(endPoint, kleisli)
        kleisli
      }
    }
}
