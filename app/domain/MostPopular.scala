package domain

import domain.HomePage.HomePageToResponse.Ok
import play.api.libs.ws.{WSClient, WSResponse}
import services.HostAndPorts
import org.validoc.utilities.debugEndpoint.MakeDebugQuery
import org.validoc.utilities.endpoint.{EndPointToRes, MakeReqFromHttpReq}
import org.validoc.utilities.kleisli.{ChildReqFinder, Enricher}
import play.api.mvc.{Request, Result, Results}
import services.objectify.{BuildFromResponse, BuildRequestFrom}

trait MostPopularQuery

case class MostPopular(programmeIds: Seq[ProgrammeId])

case class EnrichedMostPopular(programmes: Seq[Programme])

object MostPopularQuery extends MostPopularQuery {

  implicit object MakeDebugQueryForMostPopularQuery extends MakeDebugQuery[MostPopularQuery] {
    override def apply(v1: String) = MostPopularQuery
  }
  implicit object MakeEndPointQueryForMostPopularQuery extends MakeReqFromHttpReq [Request[_] , MostPopularQuery] {
    override def apply(v1: Request[_]) = MostPopularQuery
  }

  implicit object BuilderForVogueRequest extends BuildRequestFrom[MostPopularQuery] {
    override def apply(ws: WSClient)(t: MostPopularQuery)(implicit hostAndPorts: HostAndPorts) =
      ws.url(hostAndPorts.vogueHostAndPort + "/mostpopular")
  }
}

object MostPopular {

  implicit object BuildFromResponseForVogue extends BuildFromResponse[MostPopularQuery, MostPopular] {
    override def status200(req: MostPopularQuery, response: WSResponse) = MostPopular(response.body.split(",").map(id => ProgrammeId(id)))

    override def statusOther(req: MostPopularQuery, response: WSResponse) = throw new RuntimeException(s"Unexpected code from vogue: ${response}")
  }

  implicit object ChildReqFinderMostPopular extends ChildReqFinder[MostPopular, ProgrammeId] {
    override def apply(v1: MostPopular) = v1.programmeIds
  }


}

object EnrichedMostPopular {

  implicit object EnricherForMostPopular extends Enricher[MostPopularQuery, MostPopular, Programme, EnrichedMostPopular] {
    override def apply(v1: MostPopularQuery, v2: MostPopular, v3: Seq[Programme]) = EnrichedMostPopular(v3)
  }
  implicit  object EnrichedMostPopularToResponse extends Results with  EndPointToRes[Result, EnrichedMostPopular] {
    override def apply(v1: EnrichedMostPopular) = Ok(v1.toString).as("text/html")
  }

} 