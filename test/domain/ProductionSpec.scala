package domain

class ProductionSpec extends UnitSpec with BuildFromResponseFixture[ProductionId, Production] with BuildRequestFromFixture[ProductionId] {

  val productionId = ProductionId("someId")

  behavior of "Production"


  it should "have a BuildRequest which turns a production ID to WsRequest" in {
    setupRequest { (client, request, buildRequestFrom) =>
      (client.url _) when "fnordHp/production/someId" returns request
      buildRequestFrom(client)(productionId) shouldBe request
    }
  }

  it should "have a BuildFromResponse which makes a Production" in {
    setupResponse("someStuff", 200) { (response, builderFromResponse) =>
      builderFromResponse(productionId)(response) shouldBe Production(productionId, "someStuff")
    }
  }

  it should "have a BuildFromResponse that throws an error when none 200 status code" in {
    setupResponse("someStuff", 201) { (response, builderFromResponse) =>
      intercept[RuntimeException](builderFromResponse(productionId)(response))
    }
  }

}
