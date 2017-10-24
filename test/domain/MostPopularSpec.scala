package domain

import org.validoc.utilities.{TraceId, UnitSpec}
import org.validoc.utilities.kleisli.Kleislis


class MostPopularSpec extends UnitSpec with BuildRequestFromFixture[MostPopularQuery] with BuildFromResponseFixture[MostPopularQuery, MostPopular] with Kleislis {

  behavior of "MostPopularQuery"

  private val query = MostPopularQuery(TraceId("someTraceId"))
  it should "build a WsRequest" in {
    setupRequest { (wsClient, request, buildRequestFrom) =>
      (wsClient.url _) when "vogueHP/mostpopular" returns request
      buildRequestFrom(wsClient)(query) shouldBe request
    }
  }

  behavior of "MostPopular"

  it should "have a BuildFromResponse which builds a MostPopular from a WsResponse" in {
    setupResponse("1,2", 200) { (response, buildFromResponse) =>
      buildFromResponse(query)(response) shouldBe MostPopular(List(ProgrammeId("1"), ProgrammeId("2")))
    }
  }

  it should " have a BuildFromResponse which throws an Exceptions if a non 200 status code" in {
    setupResponse("1,2", 201) { (response, buildFromResponse) =>
      intercept[RuntimeException](buildFromResponse(query)(response))
    }
  }
}
