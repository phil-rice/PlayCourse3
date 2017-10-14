package services

import javax.inject.Inject

import com.google.inject.ImplementedBy
import play.api.libs.ws.{WSClient, WSRequest, WSResponse}
import utilities.kleisli.Kleisli

import scala.concurrent.Future

@ImplementedBy(classOf[MockRawHttpServices])
trait RawHttpServices {
  def vogueHttp: Kleisli[WSRequest, WSResponse]

  def billboardHttp: Kleisli[WSRequest, WSResponse]

  def fnordProductionHttp: Kleisli[WSRequest, WSResponse]

  def fnordProgrammeHttp: Kleisli[WSRequest, WSResponse]
}

class MockRawHttpServices @Inject()(wsClient: WSClient) extends RawHttpServices {
  def mockHttp(value: String => String): Kleisli[WSRequest, WSResponse] = { request: WSRequest => Future.successful(new MockWsResponse(value(request.url))) }

  def mockFnord(prefix: String) = mockHttp(uri => prefix + uri.split("/").last)

  override def vogueHttp = mockHttp(_ => "1,2,3")

  override def billboardHttp = mockHttp(_ => "4,5,6")

  override def fnordProductionHttp = mockFnord("Production_")

  override def fnordProgrammeHttp = mockFnord("Programme_")
}
