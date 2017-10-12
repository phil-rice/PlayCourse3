package utilities

import domain.{ServicesFixture, UnitSpec}
import utilities.kleisli.Kleislis

import scala.concurrent.ExecutionContext.Implicits._

class MergerSpec extends UnitSpec with ServicesFixture with Kleislis {

  behavior of "KleisliMerger"

  it should "call the findChildIds, pass these to the child services and merge the results" in {
    (mockService1, mockService2).merge[String, String].apply("mainId").await shouldBe "TestReq1(mainId)TestReq2(mainId)"
  }
}
