package domain


class PromotionSpec extends UnitSpec with BuildRequestFromFixture [PromotionQuery]with BuildFromResponseFixture[PromotionQuery, Promotion] {

  behavior of "PromotionQuery"

  it should "build a WsRequest" in {
    setupRequest { (wsClient, request, buildRequestFrom) =>
      (wsClient.url _) when "billboardHP/" returns request
      buildRequestFrom(wsClient)(PromotionQuery) shouldBe request
    }
  }

  behavior of "Promotion"

  it should "have a BuildFromResponse which builds a Promotion from a WsResponse" in {
    setupResponse("1,2", 200) { (response, buildFromResponse) =>
      buildFromResponse(PromotionQuery)(response) shouldBe Promotion(List(ProductionId("1"), ProductionId("2")))
    }
  }

  it should " have a BuildFromResponse which throws an Exceptions if a non 200 status code" in {
    setupResponse("1,2", 201) { (response, buildFromResponse) =>
      intercept[RuntimeException](buildFromResponse(PromotionQuery)(response))
    }
  }
}
