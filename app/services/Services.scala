package services

import javax.inject.{Inject, Singleton}

import domain._
import play.api.libs.ws.{WSClient, WSRequest, WSResponse}
import utilities.kleisli._
import utilities.objectify.{BuildFromResponse, BuildRequestFrom}
import utilities.profile.TryProfileData

import scala.concurrent.{ExecutionContext, Future}


case class HostAndPorts(vogueHostAndPort: String = "http://localhost", billboardHostAndPort: String = "http://localhost", fnordHostAndPort: String = "http://localhost")

object HostAndPorts {
  implicit val localhosts = HostAndPorts()
}

class Builders {
  val localhosts = HostAndPorts()

  import localhosts._

  implicit object BuilderForVogueRequest extends BuildRequestFrom[MostPopularQuery] {
    override def apply(ws: WSClient)(t: MostPopularQuery) = ws.url(vogueHostAndPort + "/mostpopular")
  }

  implicit object BuildFromResponseForVogue extends BuildFromResponse[MostPopularQuery, MostPopular] {
    //this should actually do a json story
    override def status200(req: MostPopularQuery, response: WSResponse) =
      MostPopular(response.body.split(",").map(id => ProgrammeId(id)))

    override def statusOther(req: MostPopularQuery, response: WSResponse) = throw new RuntimeException(s"Unexpected code from vogue: ${response}")
  }

  implicit object BuilderForBillboard extends BuildRequestFrom[PromotionQuery] {
    override def apply(ws: WSClient)(t: PromotionQuery) = ws.url(billboardHostAndPort + "/")
  }

  implicit object BuildFromResponseForBillBoard extends BuildFromResponse[PromotionQuery, Promotion] {
    override def status200(req: PromotionQuery, response: WSResponse) =
      Promotion(response.body.split(",").map(id => ProductionId(id)))

    override def statusOther(req: PromotionQuery, response: WSResponse) = throw new RuntimeException(s"Unexpected code from billboard: ${response}")
  }

  implicit object BuildRequestForProductionid extends BuildRequestFrom[ProductionId] {
    override def apply(ws: WSClient)(t: ProductionId) = ws.url(fnordHostAndPort + s"/production/${t.id}")
  }

  implicit object BuildFromResponseForProduction extends BuildFromResponse[ProductionId, Production] {
    override def status200(req: ProductionId, response: WSResponse) = Production(req, response.body)

    override def statusOther(req: ProductionId, response: WSResponse) = throw new RuntimeException(s"Unexpected code from fnord - production: ${response}")
  }

  implicit object BuildRequestForProgrammeid extends BuildRequestFrom[ProgrammeId] {
    override def apply(ws: WSClient)(t: ProgrammeId) = ws.url(fnordHostAndPort + s"/programme/${t.id}")
  }

  implicit object BuildFromResponseForProgramme extends BuildFromResponse[ProgrammeId, Programme] {
    override def status200(req: ProgrammeId, response: WSResponse) = Programme(req, response.body)

    override def statusOther(req: ProgrammeId, response: WSResponse) = throw new RuntimeException(s"Unexpected code from fnord - programme: ${response}")
  }

  implicit object ChildReqFinderPromotions extends ChildReqFinder[Promotion, ProductionId] {
    override def apply(v1: Promotion) = v1.productionIds
  }

  implicit object ChildReqFinderMostPopular extends ChildReqFinder[MostPopular, ProgrammeId] {
    override def apply(v1: MostPopular) = v1.programmeIds
  }

  implicit object EnricherForPromotions extends Enricher[PromotionQuery, Promotion, Production, EnrichedPromotion] {
    override def apply(v1: PromotionQuery, v2: Promotion) =
    { productions: Seq[Production] => EnrichedPromotion(productions) }
  }

  implicit object EnricherForMostPopular extends Enricher[MostPopularQuery, MostPopular, Programme, EnrichedMostPopular] {
    override def apply(v1: MostPopularQuery, v2: MostPopular) =
    { programs: Seq[Programme] => EnrichedMostPopular(programs) }
  }

  implicit object FindChildIdForHomePageAndMostPopular extends FindChildId[HomePageQuery, MostPopularQuery] {
    override def apply(v1: HomePageQuery) = MostPopularQuery
  }

  implicit object FindChildIdForHomePageAndPromotion extends FindChildId[HomePageQuery, PromotionQuery] {
    override def apply(v1: HomePageQuery) = PromotionQuery
  }

  implicit object HomePageMerger extends Merge[EnrichedPromotion, EnrichedMostPopular, HomePage] {
    override def apply(v1: EnrichedPromotion, v2: EnrichedMostPopular) = HomePage(v2, v1)
  }

}

case class RawHttpServices(vogueHttp: Kleisli[WSRequest, WSResponse], billboardHttp: Kleisli[WSRequest, WSResponse], fnordProductionHttp: Kleisli[WSRequest, WSResponse], fnordProgrammeHttp: Kleisli[WSRequest, WSResponse])

object RawHttpServices {
  def mockHttp(value: String => String): Kleisli[WSRequest, WSResponse] = { request: WSRequest =>
    println(s"mockHttp $request")
    val mock: WSResponse = new WSResponse {
      override def body = value(request.url)

      override def bodyAsBytes = ???

      override def cookies = ???

      override def xml = ???

      override def json = ???

      override def headers = ???

      override def cookie(name: String) = ???

      override def underlying[T] = ???

      override def bodyAsSource = ???

      override def allHeaders = ???

      override def statusText = ???

      override def status = 200
    }
    Future.successful(mock)
  }

  def mockFnord(prefix: String) = mockHttp(uri => prefix + uri.split("/").last)

  implicit val forTests = new RawHttpServices(mockHttp(_ => "1,2,3"), mockHttp(_ => "4,5,6"), mockFnord("Production_"), mockFnord("Programme_"))
}

@Singleton()
class Services @Inject()(implicit wSClient: WSClient, builders: Builders, ex: ExecutionContext) {

  import builders._
  import utilities.kleisli.Kleislis._
  import RawHttpServices.forTests._

  val vogueProfileData = new TryProfileData
  val billboardProfileData = new TryProfileData
  val fnordProductionProfileData = new TryProfileData
  val fnordProgrammeProfileData = new TryProfileData


  val vogue: Kleisli[MostPopularQuery, MostPopular] = new KleisliPimper(vogueHttp) |+| profile(vogueProfileData) |+| objectify[MostPopularQuery, MostPopular] |+| cache
  val billboard: Kleisli[PromotionQuery, Promotion] = new KleisliPimper(billboardHttp) |+| profile(billboardProfileData) |+| objectify[PromotionQuery, Promotion] |+| cache
  val productionFnord: Kleisli[ProductionId, Production] = new KleisliPimper(fnordProductionHttp) |+| profile(fnordProductionProfileData) |+| objectify[ProductionId, Production]
  val programmeFnord: Kleisli[ProgrammeId, Programme] = new KleisliPimper(fnordProgrammeHttp) |+| profile(fnordProgrammeProfileData) |+| objectify[ProgrammeId, Programme]


  val enrichedPromotion = (billboard, productionFnord).enrich[EnrichedPromotion]
  val enrichedMostPopular = (vogue, programmeFnord).enrich[EnrichedMostPopular]

  val homePage = new MergerTupleFinder[PromotionQuery, EnrichedPromotion, MostPopularQuery, EnrichedMostPopular]((enrichedPromotion, enrichedMostPopular)).merge[HomePageQuery, HomePage]


}
