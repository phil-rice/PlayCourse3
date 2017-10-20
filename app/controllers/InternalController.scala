package controllers

import javax.inject.{Inject, Singleton}

import domain._
import play.api.mvc.{AbstractController, ControllerComponents}
import services.Services

import scala.concurrent.ExecutionContext


@Singleton
class InternalController @Inject()(cc: ControllerComponents, services: Services) extends AbstractController(cc) {
  implicit val ec = ExecutionContext.global

  def index = Action { implicit request =>
    Ok(<html>
      <body>
        {services.serviceTrees.}
      </body>
    </html>).as("text/html")

  }

  def allServices(name: String, param: String) = Action.async { implicit request =>
    services.allDebugEndPoints(name)(param).map(result => Ok(result).as("text/html"))
  }

}
