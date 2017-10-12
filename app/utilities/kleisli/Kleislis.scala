package utilities.kleisli

import play.api.libs.ws.{WSRequest, WSResponse}
import utilities.cache.{CacheLanguage, NeverEndingCache}
import utilities.objectify.ObjectifyLanguage
import utilities.profile.ProfilingLanguage

import scala.concurrent.{ExecutionContext, Future}

trait Merge[Res1, Res2, MainRes] extends ((Res1, Res2) => MainRes)

trait FindChildId[Parent, ChildReq] extends (Parent => ChildReq)

trait ChildReqFinder[Parent, ChildReq] extends (Parent => Seq[ChildReq])

trait Enricher[ParentReq, ParentRes, ChildRes, EnrichedParent] extends
  ((ParentReq, ParentRes, Seq[ChildRes]) => EnrichedParent)


trait Kleislis extends CacheLanguage with ProfilingLanguage with ObjectifyLanguage {
  def http: Kleisli[WSRequest, WSResponse] = { httpRequest: WSRequest => httpRequest.execute() }

  implicit class KleisliPimper[Req, Res](k: Req => Future[Res]) {
    def |+|[Req2, Res2](tr: KleisliTransformer[Req, Res, Req2, Res2]) = tr(k)
  }

  implicit class TupleOfKleisliPimper[Req1, Res1, Req2, Res2](tuple: (Kleisli[Req1, Res1], Kleisli[Req2, Res2]))(implicit ex: ExecutionContext) {
    val one = tuple._1
    val two = tuple._2

    def merge[MainReq, MainRes](implicit findId1: FindChildId[MainReq, Req1], findId2: FindChildId[MainReq, Req2], merge: Merge[Res1, Res2, MainRes]): Kleisli[MainReq, MainRes] = { main: MainReq =>
      val f1 = one(findId1(main))
      val f2 = two(findId2(main))
      for {v1 <- f1; v2 <- f2} yield merge(v1, v2)
    }

    def enrich[EnrichedParent](implicit childReqFinder: ChildReqFinder[Res1, Req2],
                               enricher: Enricher[Req1, Res1, Res2, EnrichedParent]): Kleisli[Req1, EnrichedParent] = { req =>
      for {
        parent <- one(req)
        childIds = childReqFinder(parent)
        children <- Future.sequence(childIds.map(two))
      } yield enricher(req, parent, children)
    }
  }

}

object Kleislis extends Kleislis
