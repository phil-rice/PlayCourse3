package domain

import play.api.libs.ws.{WSClient, WSResponse}
import services.HostAndPorts
import utilities.debugEndpoint.MakeDebugQuery
import utilities.kleisli.{ChildReqFinder, Enricher}
import utilities.objectify.{BuildFromResponse, BuildRequestFrom}

trait MostPopularQuery

case class MostPopular(programmeIds: Seq[ProgrammeId])

case class EnrichedMostPopular(programmes: Seq[Programme])

object MostPopularQuery extends MostPopularQuery {

  implicit object MakeDebugQueryForMostPopularQuery extends MakeDebugQuery[MostPopularQuery] {
    override def apply(v1: String) = MostPopularQuery
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

}