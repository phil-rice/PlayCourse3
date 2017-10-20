package org.validoc.utilities.endpoint

import org.validoc.utilities.debugEndpoint.{DebugToString, MakeDebugQuery}
import org.validoc.utilities.kleisli.KleisliDelegate
import utilities.kleisli.Kleisli
import org.validoc.utilities.Arrows._
import scala.concurrent.ExecutionContext
import org.validoc.utilities.kleisli.Kleislis._
trait MakeReqFromHttpReq[HttpReq, Req] extends (HttpReq => Req)

trait EndPointToRes[HttpRes, T] extends (T => HttpRes)

trait EndPointLanguage [HttpReq, HttpRes]{

  var allEndPoints = Map[String, Kleisli[HttpReq, HttpRes]]()


  def endPoint[ Req, Res](name: String)(implicit makeQuery: MakeReqFromHttpReq[HttpReq, Req], endpointToHttpRes: EndPointToRes[HttpRes, Res], ex: ExecutionContext): KleisliDelegate[Req, Res] =
    new KleisliDelegate[Req, Res] {
      override def apply(kleisli: Kleisli[Req, Res]): Kleisli[Req, Res] = {
        allEndPoints = allEndPoints + (name -> (makeQuery ~> kleisli ~> endpointToHttpRes))
        kleisli
      }
    }
}
