package domain

import play.api.libs.ws.{WSClient, WSResponse}
import services.HostAndPorts
import utilities.kleisli.{ChildReqFinder, Enricher}
import utilities.objectify.{BuildFromResponse, BuildRequestFrom}

trait MostPopularQuery

case class MostPopular(programmeIds: Seq[ProgrammeId])

case class EnrichedMostPopular(programmes: Seq[Programme])

object MostPopularQuery extends MostPopularQuery {

}

object MostPopular {


  implicit object ChildReqFinderMostPopular extends ChildReqFinder[MostPopular, ProgrammeId] {
    override def apply(v1: MostPopular) = v1.programmeIds
  }

}

object EnrichedMostPopular {

  implicit object EnricherForMostPopular extends Enricher[MostPopularQuery, MostPopular, Programme, EnrichedMostPopular] {
    override def apply(v1: MostPopularQuery, v2: MostPopular) = { programs: Seq[Programme] => EnrichedMostPopular(programs) }
  }

}