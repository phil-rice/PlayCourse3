package domain

import org.scalamock.scalatest.MockFactory
import org.validoc.utilities.kleisli.Enricher
import play.api.libs.ws.{WSClient, WSRequest, WSResponse}
import services.HostAndPorts
import utilities.objectify.{BuildFromResponse, BuildRequestFrom}

trait BuildRequestFromFixture[Req] extends MockFactory {

  implicit val hostAndPorts = HostAndPorts("vogueHP", "billboardHP", "fnordHp")

  def setupRequest(fn: (WSClient, WSRequest, BuildRequestFrom[Req]) => Unit)(implicit buildRequestFrom: BuildRequestFrom[Req]): Unit = {
    val client = stub[WSClient]
    val request = stub[WSRequest]
    fn(client, request, buildRequestFrom)
  }
}

trait BuildFromResponseFixture[Req, Res] extends MockFactory {
  def setupResponse(string: String, code: Int)(fn: (WSResponse, BuildFromResponse[Req, Res]) => Unit)(implicit buildFromResponse: BuildFromResponse[Req, Res]) = {
    val response = stub[WSResponse]
    (response.body _) when() returns string
    (response.status _) when() returns code
    fn(response, buildFromResponse)
  }
}

trait EnricherFixture[ParentReq, ParentRes, ChildRes, EnrichedParent] extends MockFactory {
  def setupEnricher(fn: Enricher[ParentReq, ParentRes, ChildRes, EnrichedParent] => Unit)(implicit enricher: Enricher[ParentReq, ParentRes, ChildRes, EnrichedParent]) = {
    fn(enricher)
  }
}
