package domain

import utilities.objectify.BuildRequestFrom

class ProgrammeSpec extends UnitSpec with BuildRequestFromFixture[ProgrammeId] with BuildFromResponseFixture[ProgrammeId, Programme] {

  behavior of "ProgrammeId"
  val programmeId = ProgrammeId("someId")

  it should "have a BuildRequestFrom" in {
    setupRequest { (client, request, buildRequestFrom) =>
      (client.url _) when "fnordHp/programme/someId" returns request
      buildRequestFrom(client)(programmeId) shouldBe request
    }
  }

  it should "have a BuildFromResponse" in {
    setupResponse("someStuff", 200) { (response, buildFromResponse) =>
      buildFromResponse(programmeId)(response) shouldBe Programme(programmeId, "someStuff")
    }
  }
  it should "have a BuildFromResponse that throws an exception for none 200 status codes" in {
    setupResponse("someStuff", 201) { (response, buildFromResponse) =>
      intercept[RuntimeException](buildFromResponse(programmeId)(response))
    }
  }
}
