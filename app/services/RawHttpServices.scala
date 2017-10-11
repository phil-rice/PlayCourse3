package services

import play.api.libs.ws.{WSRequest, WSResponse}
import utilities.kleisli.Kleisli

import scala.concurrent.Future

case class RawHttpServices(vogueHttp: Kleisli[WSRequest, WSResponse], billboardHttp: Kleisli[WSRequest, WSResponse], fnordProductionHttp: Kleisli[WSRequest, WSResponse], fnordProgrammeHttp: Kleisli[WSRequest, WSResponse])

object RawHttpServices {
  def mockHttp(value: String => String): Kleisli[WSRequest, WSResponse] = { request: WSRequest =>
    println(s"mockHttp $request")
    val mock: WSResponse = new WSResponse {
      override def body = value(request.url)

      override def bodyAsBytes = ???

      override def cookies = ???

      override def xml = ???

      override def json = ???

      override def headers = ???

      override def cookie(name: String) = ???

      override def underlying[T] = ???

      override def bodyAsSource = ???

      override def allHeaders = ???

      override def statusText = ???

      override def status = 200
    }
    Future.successful(mock)
  }

  def mockFnord(prefix: String) = mockHttp(uri => prefix + uri.split("/").last)

  implicit val forTests = new RawHttpServices(mockHttp(_ => "1,2,3"), mockHttp(_ => "4,5,6"), mockFnord("Production_"), mockFnord("Programme_"))
}