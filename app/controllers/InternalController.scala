package controllers

import javax.inject.{Inject, Singleton}

import org.validoc.utilities._
import org.validoc.utilities.cache.Cache
import org.validoc.utilities.debugEndpoint.DebugEndPoint
import org.validoc.utilities.endpoint.EndPoint
import play.api.mvc.{AbstractController, ControllerComponents, Request, Result}
import services.Services
import utilities.kleisli.Kleisli

import scala.concurrent.ExecutionContext
import scala.reflect.ClassTag
import scala.xml.{EntityRef, NodeSeq}


@Singleton
class InternalController @Inject()(cc: ControllerComponents, services: Services) extends AbstractController(cc) {
  implicit val ec = ExecutionContext.global

  import AsHtml.AsHtmlPimper

  def addToMap[ST <: ServiceType : ClassTag, Req: ClassTag, Res: ClassTag](map: Map[Kleisli[_, _], String], getService: (Kleisli[_, _], ST) => Kleisli[_, _], getName: ST => String) =
    services.serviceTrees.servicesWithSome[ST, Req, Res].foldLeft(map) { case (acc, (st, service)) =>
      acc + (getService(service, st) -> getName(st))
    }

  val mapServiceToEndPoint = addToMap[EndPoint, Request[_], Result](addToMap[DebugEndPoint, String, String](Map(), (s, st) => st.actualEndPoint, "/internal/" + _.name), (s, st) => s, _.name);

  //  services.serviceTrees.servicesWithSome[DebugEndPoint, String, String].foldLeft(Map[Kleisli[_, _], String]()) { case (acc, (st, service)) =>
  //    acc + (st.actualEndPoint -> st.name)
  //  }

  def wrapInOptionalLink(tree: Tree)(nodeSeq: NodeSeq) =
    mapServiceToEndPoint.get(tree.service) match {
      case Some(name) => <a href={name}>
        {nodeSeq}
      </a>
      case None => nodeSeq
    }

  def index = Action { implicit request =>
    Ok(<html>
      <body>
        <table>
          {new ServiceTreeAsMap(services.serviceTrees).servicesAsList[EndPoint, Request[_], Result].map { t: IndentAnd[Tree] =>
          <tr>
            <td>
              {NodeSeq.fromSeq((List.fill(t.indent * 4)(EntityRef("nbsp")) ::: wrapInOptionalLink(t.t)(t.t.asHtml).toList).flatten)}
            </td>
          </tr>
        }}
        </table>
      </body>
    </html>).as("text/html")
  }

  val allCaches = services.serviceTrees.servicesWith[Cache[Any, Any], Any, Any]

  def caches = Action { implicit request =>
    Ok(<html>
      <body>
        <ul>
          {allCaches.map { c => s"size ${c.size}" }}
        </ul>
      </body>
    </html>
    ).as("text/html")
  }

  val debugEndPoints = services.serviceTrees.toMap[DebugEndPoint, String, String](_.name)

  def allServices(name: String, param: String) = Action.async { implicit request =>
    debugEndPoints(name)(param).map(result => Ok(result).as("text/html"))
  }

}
