package services.objectify

import org.validoc.utilities.{ServiceTrees, ServiceType}
import org.validoc.utilities.kleisli.KleisliTransformer
import play.api.libs.ws.{WSClient, WSRequest, WSResponse}
import services.HostAndPorts
import utilities.kleisli.Kleisli

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

trait BuildRequestFrom[Req] {
  def apply(ws: WSClient)(t: Req)(implicit hostAndPorts: HostAndPorts): WSRequest
}

trait BuildFromResponse[Req, Res] {
  def apply(req: Req)(response: WSResponse): Res = response.status match {
    case 200 => status200(req, response)
    case x => statusOther(req, response)
  }

  def status200(req: Req, response: WSResponse): Res

  def statusOther(req: Req, response: WSResponse): Res
}

class Objectify[Req, Res](ws: WSClient, delegate: WSRequest => Future[WSResponse])(implicit ex: ExecutionContext, buildRequest: BuildRequestFrom[Req], buildFromResponse: BuildFromResponse[Req, Res]) extends (Req => Future[Res]) {
  val makeRequest = buildRequest(ws) _

  override def apply(req: Req): Future[Res] = delegate(makeRequest(req)).map(buildFromResponse(req))
}

trait ObjectifyServiceType extends ServiceType

trait ObjectifyLanguage {

  def objectify[Req: BuildRequestFrom : ClassTag, Res: ClassTag](implicit wSClient: WSClient, ex: ExecutionContext, buildFromResponse: BuildFromResponse[Req, Res], serviceTrees: ServiceTrees) = new KleisliTransformer[WSRequest, WSResponse, Req, Res] {
    override def apply(v1: Kleisli[WSRequest, WSResponse]) = serviceTrees.add[ObjectifyServiceType].addOneChild(new Objectify[Req, Res](wSClient, v1), v1)
  }

}