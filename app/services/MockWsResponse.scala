package services

import play.api.libs.ws.WSResponse

class MockWsResponse(value: String) extends WSResponse {
  override def body = value

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