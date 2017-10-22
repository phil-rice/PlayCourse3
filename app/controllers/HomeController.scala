package controllers

import javax.inject._

import org.validoc.utilities.debugEndpoint.DebugEndPoint
import org.validoc.utilities.endpoint.EndPoint
import play.api.mvc._
import services.Services

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class HomeController @Inject()(cc: ControllerComponents, services: Services) extends AbstractController(cc) {

  /**
    * Create an Action to render an HTML page with a welcome message.
    * The configuration in the `routes` file means that this method
    * will be called when the application receives a `GET` request with
    * a path of `/`.
    */
  def index = Action {
    Ok("").as("text/html")
  }

  val allEndPoints = services.serviceTrees.toMap[EndPoint, Request[_], Result](_.name)

  def allServices(name: String) = Action.async { implicit request =>
    allEndPoints(name)(request)
  }

}
