package domain

import play.api.libs.ws.{WSClient, WSResponse}
import services.HostAndPorts
import org.validoc.utilities.debugEndpoint.MakeDebugQuery
import services.objectify.{BuildFromResponse, BuildRequestFrom}

case class ProgrammeId(id: String) extends AnyVal

case class Programme(id: ProgrammeId, info: String)

object ProgrammeId {

  implicit object MakeDebugQueryForProgrammeId extends MakeDebugQuery[ProgrammeId] {
    override def apply(v1: String) = ProgrammeId(v1)
  }

  implicit object BuildRequestForProgrammeid extends BuildRequestFrom[ProgrammeId] {
    override def apply(ws: WSClient)(t: ProgrammeId)(implicit hostAndPorts: HostAndPorts) = ws.url(hostAndPorts.fnordHostAndPort + s"/programme/${t.id}")
  }

}

object Programme {

  implicit object BuildFromResponseForProgramme extends BuildFromResponse[ProgrammeId, Programme] {
    override def status200(req: ProgrammeId, response: WSResponse) = Programme(req, response.body)

    override def statusOther(req: ProgrammeId, response: WSResponse) = throw new RuntimeException(s"Unexpected code from fnord - programme: ${response}")
  }

}