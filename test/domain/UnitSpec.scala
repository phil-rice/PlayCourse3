package domain

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}
import play.api.libs.ws.{WSClient, WSRequest, WSResponse}
import services.HostAndPorts
import utilities.kleisli._
import utilities.objectify.{BuildFromResponse, BuildRequestFrom}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

trait UnitSpec extends FlatSpec with Matchers with MockFactory {

  implicit class FuturePimper[T](f: Future[T]) {
    def await = Await.result(f, 5 seconds)
  }

}

trait BuildRequestFromFixture[Req] extends MockFactory {

  implicit val hostAndPorts = HostAndPorts("vogueHP", "billboardHP", "fnordHp")

  def setupRequest(fn: (WSClient, WSRequest, BuildRequestFrom[Req]) => Unit)(implicit buildRequestFrom: BuildRequestFrom[Req]): Unit = {
    val client = stub[WSClient]
    val request = stub[WSRequest]
    fn(client, request, buildRequestFrom)
  }
}

trait BuildFromResponseFixture[Req, Res] extends MockFactory {
  def setupResponse(string: String, code: Int)(fn: (WSResponse, BuildFromResponse[Req, Res]) => Unit)(implicit buildFromResponse: BuildFromResponse[Req, Res]) = {
    val response = stub[WSResponse]
    (response.body _) when() returns string
    (response.status _) when() returns code
    fn(response, buildFromResponse)
  }
}

trait EnricherFixture[ParentReq, ParentRes, ChildRes, EnrichedParent] extends MockFactory {
  def setupEnricher(fn: Enricher[ParentReq, ParentRes, ChildRes, EnrichedParent] => Unit)(implicit enricher: Enricher[ParentReq, ParentRes, ChildRes, EnrichedParent]) = {
    fn(enricher)
  }
}


trait MergerFixture[MainReq, MainRes, Res1, Res2] {
  def setupFindChildId[Req](fn: FindChildId[MainReq, Req] => Unit)(implicit findChildId: FindChildId[MainReq, Req]) = fn(findChildId)

  def setupMerger(fn: Merge[Res1, Res2, MainRes] => Unit)(implicit merge: Merge[Res1, Res2, MainRes]) = fn(merge)
}

case class TestReq1(s: String)

case class TestReq2(s: String)

case class TestParentRes(s: List[TestReq1])

case class TestEnrichedRes(s: List[TestReq1])

trait ServicesFixture {

  val mockParentService: Kleisli[String, TestParentRes] = { req: String => Future.successful(TestParentRes(req.split(",").map(TestReq1(_)).toList)) }

  val mockService1: Kleisli[TestReq1, String] = {
    req: TestReq1 => Future.successful(req.toString)
  }
  val mockService2: Kleisli[TestReq2, String] = {
    req: TestReq2 => Future.successful(req.toString)
  }

  implicit object findChildIdForReq1Test extends FindChildId[String, TestReq1] {
    override def apply(v1: String) = TestReq1(v1)
  }

  implicit object findChildIdForReq2Test extends FindChildId[String, TestReq2] {
    override def apply(v1: String) = TestReq2(v1)
  }

  implicit object ChildReqFinder1 extends ChildReqFinder[TestParentRes, TestReq1] {
    override def apply(v1: TestParentRes) = v1.s
  }


  implicit object MergerForTest extends Merge[String, String, String] {
    override def apply(v1: String, v2: String) = v1 + v2
  }

  implicit object EnricherForTest extends Enricher[String, TestParentRes, String, String] {
    override def apply(v1: String, v2: TestParentRes, v3: Seq[String]) = v1 + "/" + v2 + "/" + v3
  }

}
