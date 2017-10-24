package domain

import java.time.LocalDateTime

import org.validoc.utilities.{HasTraceId, TraceId, TraceIdGenerator}
import org.validoc.utilities.debugEndpoint.MakeDebugQuery
import org.validoc.utilities.endpoint.{EndPointToRes, MakeReqFromHttpReq}
import org.validoc.utilities.kleisli.{FindChildId, Merge}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.mvc.{Request, Result, Results}
import play.mvc.Http.Response
import org.validoc.utilities.Arrows._
import org.validoc.utilities.cache.ByPassCache

case class HomePageQuery(traceId: TraceId, bypassCache: Boolean)

object HomePageQuery {

  implicit object BypassCacheForHomePageQuery extends ByPassCache[HomePageQuery] {
    override def apply(v1: HomePageQuery) = v1.bypassCache
  }

  implicit def makeDebugQueryForHomePageQuery(implicit traceIdGenerator: TraceIdGenerator) = new MakeDebugQuery[HomePageQuery] {
    override def apply(v1: String) = HomePageQuery(traceIdGenerator(), true)
  }

  implicit def MakeEndPointQueryForHomePageQuery[Content](implicit traceIdGenerator: TraceIdGenerator, hasTraceId: HasTraceId[Request[Content]]) = new MakeReqFromHttpReq[Request[Content], HomePageQuery] {
    override def apply(v1: Request[Content]) = HomePageQuery(traceIdGenerator.getOrCreate(v1), v1.getQueryString("bypassCacge").contains("true"))

  }

  implicit object FindChildIdForHomePageAndMostPopular extends FindChildId[HomePageQuery, MostPopularQuery] {
    override def apply(v1: HomePageQuery) = MostPopularQuery(v1.traceId)
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

  implicit object HomePageToResponse extends Results with EndPointToRes[Result, HomePage] {
    override def apply(v1: HomePage) = Ok(v1.toString).as("text/html")
  }

}