package utilities

import domain.{ServicesFixture, TestReq1, TestReq2, UnitSpec}
import utilities.kleisli.{Kleisli, Kleislis}

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future

class MergerSpec extends UnitSpec with ServicesFixture with Kleislis {

  behavior of "KleisliMerger"

  it should "call the findChildIds, pass these to the child services and merge the results" in {

    val mockService1: Kleisli[TestReq1, String] = {
      req: TestReq1 => Future.successful(req.toString)
    }
    val mockService2: Kleisli[TestReq1, String] = {
      req: TestReq1 => Future.successful(req.toString)
    }

    (mockService1, mockService2).shortcircuit




    (mockService1, mockService2).merge[String, String].apply("mainId").await shouldBe "TestReq1(mainId)TestReq2(mainId)"
  }
}
