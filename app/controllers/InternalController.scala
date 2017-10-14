package controllers

import javax.inject.{Inject, Singleton}

import domain._
import play.api.mvc.{AbstractController, ControllerComponents}
import services.Services

import scala.concurrent.ExecutionContext


@Singleton
class InternalController @Inject()(cc: ControllerComponents, services: Services) extends AbstractController(cc) {
  implicit val ec = ExecutionContext.global

  def allServices(name: String, param: String) = Action.async { implicit request =>
    services.debugEndpoints(name)(param).map(result => Ok(result).as("text/html"))
  }

}
