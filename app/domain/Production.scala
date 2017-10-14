package domain

import play.api.libs.ws.{WSClient, WSResponse}
import services.HostAndPorts
import utilities.debugEndpoint.MakeDebugQuery
import utilities.objectify.{BuildFromResponse, BuildRequestFrom}


case class ProductionId(id: String) extends AnyVal

case class Production(id: ProductionId, info: String)


object ProductionId {

  implicit object BuildRequestForProductionid extends BuildRequestFrom[ProductionId] {
    override def apply(ws: WSClient)(t: ProductionId)(implicit hostAndPorts: HostAndPorts) = ws.url(hostAndPorts.fnordHostAndPort + s"/production/${t.id}")
  }
  implicit object MakeDebugQueryForProductionId extends MakeDebugQuery[ProductionId] {
    override def apply(v1: String) = ProductionId(v1)
  }

}

object Production {

  implicit object BuildFromResponseForProduction extends BuildFromResponse[ProductionId, Production] {
    override def status200(req: ProductionId, response: WSResponse) = Production(req, response.body)

    override def statusOther(req: ProductionId, response: WSResponse) = throw new RuntimeException(s"Unexpected code from fnord - production: ${response}")
  }


}