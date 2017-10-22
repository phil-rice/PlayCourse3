package services

import javax.inject.{Inject, Singleton}

import domain._
import org.validoc.utilities.{MutableServiceTrees, ServiceTrees, ServiceType}
import org.validoc.utilities.debugEndpoint.DebugEndPointLanguage
import org.validoc.utilities.endpoint.EndPointLanguage
import org.validoc.utilities.kleisli.{FindChildId, Merge}
import org.validoc.utilities.profile.TryProfileData
import play.api.libs.ws.{WSClient, WSRequest, WSResponse}
import play.api.mvc.{AnyContent, Request, Result}
import play.mvc.Http.Response
import utilities.kleisli.Kleisli
import services.objectify.ObjectifyLanguage

import scala.concurrent.{ExecutionContext, Future}

trait HttpService extends ServiceType

@Singleton()
class Services @Inject()(implicit wSClient: WSClient, ex: ExecutionContext, rawHttpServices: RawHttpServices) extends ObjectifyLanguage with DebugEndPointLanguage with EndPointLanguage[Request[_], Result] {
  implicit val serviceTrees = new MutableServiceTrees()

  import rawHttpServices._
  import org.validoc.utilities.kleisli.Kleislis._

  //  def http: Kleisli[WSRequest, WSResponse] = { httpRequest: WSRequest => httpRequest.execute() }

  val vogueProfileData = new TryProfileData
  val billboardProfileData = new TryProfileData
  val fnordProductionProfileData = new TryProfileData
  val fnordProgrammeProfileData = new TryProfileData

  serviceTrees.addRoots[HttpService, WSRequest, WSResponse](vogueHttp, billboardHttp, fnordProductionHttp, fnordProgrammeHttp)

  println("Vogue: " + serviceTrees.treeForService(vogueHttp))
  val vogue: Kleisli[MostPopularQuery, MostPopular] = vogueHttp |+|
    profile(vogueProfileData) |+|
    objectify[MostPopularQuery, MostPopular] |+|
    cache |+|
    debug("vogue")

  val billboard: Kleisli[PromotionQuery, Promotion] = billboardHttp |+| profile(billboardProfileData) |+| objectify[PromotionQuery, Promotion] |+| cache |+| debug("billboard")

  val productionFnord: Kleisli[ProductionId, Production] = fnordProductionHttp |+| profile(fnordProductionProfileData) |+| objectify[ProductionId, Production] |+| debug("production")

  val programmeFnord: Kleisli[ProgrammeId, Programme] = fnordProgrammeHttp |+| profile(fnordProgrammeProfileData) |+| objectify[ProgrammeId, Programme] |+| debug("programme")

  val enrichedPromotion: Kleisli[PromotionQuery, EnrichedPromotion] = combine(billboard, productionFnord).enrich[EnrichedPromotion] |+| debug("enriched_promotion")
  val enrichedMostPopular: Kleisli[MostPopularQuery, EnrichedMostPopular] = combine(vogue, programmeFnord).enrich[EnrichedMostPopular] |+| debug("enriched_most_popular") |+| endPoint("most_popular")
  val homePage: Kleisli[HomePageQuery, HomePage] = combine(enrichedPromotion, enrichedMostPopular).merge[HomePageQuery, HomePage] |+| debug("home_page") |+| endPoint("homepage")

}
