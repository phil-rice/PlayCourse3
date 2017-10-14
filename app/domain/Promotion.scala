package domain

import play.api.libs.ws.{WSClient, WSResponse}
import services.HostAndPorts
import org.validoc.utilities.debugEndpoint.MakeDebugQuery
import org.validoc.utilities.kleisli.{ChildReqFinder, Enricher}
import services.objectify.{BuildFromResponse, BuildRequestFrom}

trait PromotionQuery

case class Promotion(productionIds: Seq[ProductionId])

case class EnrichedPromotion(productions: Seq[Production])

object PromotionQuery extends PromotionQuery {
  implicit object BuilderForBillboard extends BuildRequestFrom[PromotionQuery] {
    override def apply(ws: WSClient)(t: PromotionQuery)(implicit hostAndPorts: HostAndPorts) = ws.url(hostAndPorts.billboardHostAndPort + "/")
  }
  implicit object MakeDebugQueryForPromotionQuery extends MakeDebugQuery[PromotionQuery] {
    override def apply(v1: String) = PromotionQuery
  }

}

object Promotion {
  implicit object BuildFromResponseForBillBoard extends BuildFromResponse[PromotionQuery, Promotion] {
    override def status200(req: PromotionQuery, response: WSResponse) =
      Promotion(response.body.split(",").map(id => ProductionId(id)))

    override def statusOther(req: PromotionQuery, response: WSResponse) = throw new RuntimeException(s"Unexpected code from billboard: ${response}")
  }

  implicit object ChildReqFinderPromotions extends ChildReqFinder[Promotion, ProductionId] {
    override def apply(v1: Promotion) = v1.productionIds
  }
}

object EnrichedPromotion {
  implicit object EnricherForPromotions extends Enricher[PromotionQuery, Promotion, Production, EnrichedPromotion] {
    override def apply(v1: PromotionQuery, v2: Promotion, v3: Seq[Production]) = EnrichedPromotion(v3)
  }
}