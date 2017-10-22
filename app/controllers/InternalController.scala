package controllers

import javax.inject.{Inject, Singleton}

import domain._
import org.validoc.utilities.{AsHtml, IndentAnd, ServiceTreeAsMap, Tree}
import org.validoc.utilities.debugEndpoint.DebugEndPoint
import play.api.mvc.{AbstractController, ControllerComponents}
import services.Services
import utilities.kleisli.Kleisli

import scala.concurrent.ExecutionContext


@Singleton
class InternalController @Inject()(cc: ControllerComponents, services: Services) extends AbstractController(cc) {
  implicit val ec = ExecutionContext.global

  import AsHtml.AsHtmlPimper


  import IndentAnd.defaultAsHtml

  def index = Action { implicit request =>
    Ok(<html>
      <body>
        <ul>
          {new ServiceTreeAsMap(services.serviceTrees).treeAsList.map { t: IndentAnd[Tree] =>
          <li>
            {new AsHtmlPimper[IndentAnd[Tree]](t)(defaultAsHtml).asHtml}
          </li>
        }}
        </ul>
      </body>
    </html>).as("text/html")

  }

  val debugEndPoints = services.serviceTrees.toMap[DebugEndPoint, String, String](_.name)

  def allServices(name: String, param: String) = Action.async { implicit request =>
    debugEndPoints(name)(param).map(result => Ok(result).as("text/html"))
  }

}
