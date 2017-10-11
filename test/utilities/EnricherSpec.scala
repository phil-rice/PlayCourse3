package utilities

import domain.{ServicesFixture, UnitSpec}
import utilities.kleisli.{EnricherLanguage, MergerLanguage}
import scala.concurrent.ExecutionContext.Implicits._

class EnricherSpec extends UnitSpec with ServicesFixture with EnricherLanguage {

  behavior of "Enricher"

  it should "enrich by finding the child ids in the parent res, and sending those to the child service then combining the results with an enricher" in {
    val enrichService = (mockParentService, mockService1).enrich[String]
    enrichService("1,2,3").await shouldBe "1,2,3/TestParentRes(List(TestReq1(1), TestReq1(2), TestReq1(3)))/List(TestReq1(1), TestReq1(2), TestReq1(3))"
  }
}