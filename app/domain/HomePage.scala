package domain

import org.validoc.utilities.debugEndpoint.MakeDebugQuery
import org.validoc.utilities.endpoint.{EndPointToRes, MakeReqFromHttpReq}
import org.validoc.utilities.kleisli.{FindChildId, Merge}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.mvc.{Request, Result, Results}
import play.mvc.Http.Response

trait HomePageQuery

object HomePageQuery extends HomePageQuery {

  implicit object MakeDebugQueryForHomePageQuery extends MakeDebugQuery[HomePageQuery] {
    override def apply(v1: String) = HomePageQuery
  }
  implicit object MakeEndPointQueryForHomePageQuery extends MakeReqFromHttpReq [Request[_] , HomePageQuery] {
    override def apply(v1: Request[_]) = HomePageQuery
  }

  implicit object FindChildIdForHomePageAndMostPopular extends FindChildId[HomePageQuery, MostPopularQuery] {
    override def apply(v1: HomePageQuery) = MostPopularQuery
  }

  implicit object FindChildIdForHomePageAndPromotion extends FindChildId[HomePageQuery, PromotionQuery] {
    override def apply(v1: HomePageQuery) = PromotionQuery
  }

}

case class HomePage(mostPopular: EnrichedMostPopular, promotions: EnrichedPromotion)

object HomePage {

  implicit object HomePageMerger extends Merge[EnrichedPromotion, EnrichedMostPopular, HomePage] {
    override def apply(v1: EnrichedPromotion, v2: EnrichedMostPopular) = HomePage(v2, v1)
  }

  implicit  object HomePageToResponse extends Results with  EndPointToRes[Result, HomePage] {
    override def apply(v1: HomePage) = Ok(v1.toString).as("text/html")
  }

}