package services

import javax.inject.{Inject, Singleton}

import domain._
import play.api.libs.ws.WSClient
import utilities.kleisli.Kleisli
import utilities.profile.TryProfileData

import scala.concurrent.ExecutionContext


@Singleton()
class Services @Inject()(implicit wSClient: WSClient, ex: ExecutionContext) {

  import RawHttpServices.forTests._
  import utilities.kleisli.Kleislis._

  val vogueProfileData = new TryProfileData
  val billboardProfileData = new TryProfileData
  val fnordProductionProfileData = new TryProfileData
  val fnordProgrammeProfileData = new TryProfileData


  val vogue: Kleisli[MostPopularQuery, MostPopular] = vogueHttp |+| profile(vogueProfileData) |+| objectify[MostPopularQuery, MostPopular] |+| cache

  val billboard: Kleisli[PromotionQuery, Promotion] = (billboardHttp) |+| profile(billboardProfileData) |+| objectify[PromotionQuery, Promotion] |+| cache

  val productionFnord: Kleisli[ProductionId, Production] = (fnordProductionHttp) |+| profile(fnordProductionProfileData) |+| objectify[ProductionId, Production]

  val programmeFnord: Kleisli[ProgrammeId, Programme] = (fnordProgrammeHttp) |+| profile(fnordProgrammeProfileData) |+| objectify[ProgrammeId, Programme]


  val enrichedPromotion: Kleisli[PromotionQuery, EnrichedPromotion] = (billboard, productionFnord).enrich[EnrichedPromotion]
  val enrichedMostPopular: Kleisli[MostPopularQuery, EnrichedMostPopular] = (vogue, programmeFnord).enrich[EnrichedMostPopular]

  val homePage: Kleisli[HomePageQuery, HomePage] = (enrichedPromotion, enrichedMostPopular).merge[HomePageQuery, HomePage]

  val debugEndpoints = Map(
    "vogue" -> (vogue |+| debug),
    "billboard" -> (billboard |+| debug),
    "production" -> (productionFnord |+| debug),
    "programme" -> (programmeFnord |+| debug),
    "enrichedPromotion" -> (enrichedPromotion |+| debug),
    "enrichedMostPopular" -> (enrichedMostPopular |+| debug),
    "homePage" -> (homePage |+| debug)
  )


}
