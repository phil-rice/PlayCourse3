package services

import javax.inject.{Inject, Singleton}

import domain._
import org.validoc.utilities.debugEndpoint.DebugEndPointLanguage
import org.validoc.utilities.endpoint.EndPointLanguage
import org.validoc.utilities.kleisli.{FindChildId, Merge}
import org.validoc.utilities.profile.TryProfileData
import play.api.libs.ws.{WSClient, WSRequest, WSResponse}
import play.api.mvc.{Request, Result}
import play.mvc.Http.Response
import utilities.kleisli.Kleisli
import services.objectify.ObjectifyLanguage

import scala.concurrent.{ExecutionContext, Future}


@Singleton()
class Services @Inject()(implicit wSClient: WSClient, ex: ExecutionContext, rawHttpServices: RawHttpServices) extends ObjectifyLanguage with DebugEndPointLanguage with EndPointLanguage[Request[_], Result] {

  import rawHttpServices._
  import org.validoc.utilities.kleisli.Kleislis._

  def http: Kleisli[WSRequest, WSResponse] = { httpRequest: WSRequest => httpRequest.execute() }

  val vogueProfileData = new TryProfileData
  val billboardProfileData = new TryProfileData
  val fnordProductionProfileData = new TryProfileData
  val fnordProgrammeProfileData = new TryProfileData

  val vogue: Kleisli[MostPopularQuery, MostPopular] = vogueHttp |+| profile(vogueProfileData) |+| objectify[MostPopularQuery, MostPopular] |+| cache |+| debug("vogue")

  val billboard: Kleisli[PromotionQuery, Promotion] = billboardHttp |+| profile(billboardProfileData) |+| objectify[PromotionQuery, Promotion] |+| cache |+| debug("billboard")

  val productionFnord: Kleisli[ProductionId, Production] = fnordProductionHttp |+| profile(fnordProductionProfileData) |+| objectify[ProductionId, Production] |+| debug("production")

  val programmeFnord: Kleisli[ProgrammeId, Programme] = fnordProgrammeHttp |+| profile(fnordProgrammeProfileData) |+| objectify[ProgrammeId, Programme] |+| debug("programme")

  val enrichedPromotion: Kleisli[PromotionQuery, EnrichedPromotion] = combine(billboard, productionFnord).enrich[EnrichedPromotion] |+| debug("enriched_promotion")
  val enrichedMostPopular: Kleisli[MostPopularQuery, EnrichedMostPopular] = combine(vogue, programmeFnord).enrich[EnrichedMostPopular] |+| debug("enriched_most_popular") |+| endPoint("most_popular")
  val homePage: Kleisli[HomePageQuery, HomePage] = combine(enrichedPromotion, enrichedMostPopular).merge[HomePageQuery, HomePage] |+| debug("home_page") |+| endPoint("homepage")

}
