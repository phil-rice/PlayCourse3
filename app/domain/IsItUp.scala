package domain

import play.api.libs.ws.{WSClient, WSRequest, WSResponse}
import play.api.mvc.{AnyContent, Request, Results}
import services.HostAndPorts
import services.objectify.{BuildFromResponse, BuildRequestFrom}

case class IsUpRequest(url: String)

object IsUpRequest {
  implicit object BuildRequestForIsUpRequest extends BuildRequestFrom[IsUpRequest] {
    override def apply(ws: WSClient)(t: IsUpRequest)(implicit hostAndPorts: HostAndPorts) = ws.url(t.url)
  }
}

case class IsUpResult(url: String, up: Boolean)

object IsUpResult {
  implicit object BuildResponseForIsUpResult extends BuildFromResponse[IsUpRequest, IsUpResult] {
    override def status200(req: IsUpRequest, response: WSResponse) = IsUpResult(req.url, true)
    override def statusOther(req: IsUpRequest, response: WSResponse) =IsUpResult(req.url, true)
  }
}
