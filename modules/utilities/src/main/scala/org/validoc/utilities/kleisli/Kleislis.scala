package org.validoc.utilities.kleisli

import org.validoc.utilities.{ServiceTrees, ServiceType}
import org.validoc.utilities.cache.CacheLanguage
import org.validoc.utilities.logging.LoggingLanguage
import org.validoc.utilities.profile.ProfilingLanguage
import utilities.kleisli.Kleisli

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag
import scala.util.Try

trait Merge[Res1, Res2, MainRes] extends ((Res1, Res2) => MainRes)

trait FindChildId[Parent, ChildReq] extends (Parent => ChildReq)

trait ChildReqFinder[Parent, ChildReq] extends (Parent => Seq[ChildReq])

trait Enricher[ParentReq, ParentRes, ChildRes, EnrichedParent] extends
  ((ParentReq, ParentRes, Seq[ChildRes]) => EnrichedParent)

case object Merge extends ServiceType

case object Enrich extends ServiceType

class Combine[Req1, Res1, Req2, Res2](one: Kleisli[Req1, Res1], two: Kleisli[Req2, Res2]) {
  def merge[MainReq: ClassTag, MainRes: ClassTag](implicit findId1: FindChildId[MainReq, Req1], findId2: FindChildId[MainReq, Req2], merge: Merge[Res1, Res2, MainRes], serviceTrees: ServiceTrees, ex: ExecutionContext): Kleisli[MainReq, MainRes] =
    serviceTrees.addService[MainReq, MainRes](Merge, { main =>
      val f1: Future[Res1] = one(findId1(main))
      val f2: Future[Res2] = two(findId2(main))
      for {v1 <- f1; v2 <- f2} yield merge(v1, v2)
    }, Seq(one, two))

  def enrich[EnrichedRes: ClassTag](implicit childReqFinder: ChildReqFinder[Res1, Req2],
                                    enricher: Enricher[Req1, Res1, Res2, EnrichedRes],
                                    serviceTrees: ServiceTrees,
                                    classTagReq1: ClassTag[Req1],
                                    executionContext: ExecutionContext): Kleisli[Req1, EnrichedRes] =
    serviceTrees.addService[Req1, EnrichedRes](Enrich, { req =>
      for {
        parent <- one(req)
        childIds = childReqFinder(parent)
        children <- Future.sequence(childIds.map(two))
      } yield enricher(req, parent, children)
    }, Seq(one, two))
}

trait Kleislis extends CacheLanguage with ProfilingLanguage with LoggingLanguage {
  def combine[Req1, Res1, Req2, Res2](one: Kleisli[Req1, Res1], two: Kleisli[Req2, Res2]) = new Combine(one, two)

  implicit class KleisliPimper[Req, Res](k: Req => Future[Res]) {
    def |+|[Req2, Res2](tr: KleisliTransformer[Req, Res, Req2, Res2]) = tr(k)

    def ~>[Res2](fn: Res => Res2)(implicit executionContext: ExecutionContext) = { r: Req => k(r).map(fn) }

    def sideeffect(fn: Try[Res] => Unit)(implicit executionContext: ExecutionContext) = { (r: Req) =>
      val result = k(r)
      result.onComplete(fn)
      result
    }

    def sideeffectWithReq(fn: (Req, Try[Res]) => Unit)(implicit executionContext: ExecutionContext) = { (r: Req) => k(r).transform { tryRes => fn(r, tryRes); tryRes } }
  }


  implicit class TupleOfSimilarKleislisPimper[Req1, Res1](tuple: (Kleisli[Req1, Res1], Kleisli[Req1, Res1]))(implicit ex: ExecutionContext) {
    val one = tuple._1
    val two = tuple._2

    def shortcircuit: Kleisli[Req1, Res1] = { req: Req1 =>
      val f1: Future[Res1] = one(req)
      val f2: Future[Res1] = two(req)
      Future.firstCompletedOf(List(f1, f2))
    }
  }

}

object Kleislis extends Kleislis
