package utilities.kleisli

import scala.concurrent.{ExecutionContext, Future}

trait ChildReqFinder[Parent, ChildReq] extends (Parent => Seq[ChildReq])

trait Enricher[ParentReq, ParentRes, ChildRes, EnrichedParent] extends
  ((ParentReq, ParentRes) => Seq[ChildRes] => EnrichedParent)

class EnricherKlesli[ParentReq, ParentRes, ChildReq, ChildRes, EnrichedParent]
(parentKleisli1: Kleisli[ParentReq, ParentRes], childKleisli: Kleisli[ChildReq, ChildRes])
(implicit childReqFinder: ChildReqFinder[ParentRes, ChildReq], enricher: Enricher[ParentReq, ParentRes, ChildRes, EnrichedParent], ex: ExecutionContext) extends Kleisli[ParentReq, EnrichedParent] {
  override def apply(parentReq: ParentReq) = {
    parentKleisli1(parentReq).flatMap { parent =>
      Future.sequence(childReqFinder(parent).map(childKleisli)).map(enricher(parentReq, parent))
    }
  }
}


trait EnricherLanguage {

  implicit class EnricherTuplePimper[ParentReq, ParentRes, ChildReq, ChildRes](tuple: (Kleisli[ParentReq, ParentRes], Kleisli[ChildReq, ChildRes])) {
    def enrich[EnrichedParent](implicit childReqFinder: ChildReqFinder[ParentRes, ChildReq], enricher: Enricher[ParentReq, ParentRes, ChildRes, EnrichedParent], ex: ExecutionContext) =
      new EnricherKlesli[ParentReq, ParentRes, ChildReq, ChildRes, EnrichedParent](tuple._1, tuple._2)
  }

}


class MergerKleisli[MainReq, MainRes, Req1, Res1, Req2, Res2](one: Kleisli[Req1, Res1], two: Kleisli[Req2, Res2])
(implicit findId1: FindChildId[MainReq, Req1], findId2: FindChildId[MainReq, Req2], merge: Merge[Res1, Res2, MainRes], ex: ExecutionContext) extends Kleisli[MainReq, MainRes] {
  override def apply(main: MainReq) = {
    val f1 = one(findId1(main))
    val f2 = two(findId2(main))
    f1.flatMap { v1 => f2.map(v2 => merge(v1, v2)) }
  }
}

trait MergerLanguage {

  implicit class MergerTupleFinder[Req1, Res1, Req2, Res2](tuple: (Kleisli[Req1, Res1], Kleisli[Req2, Res2])) {
    def merge[MainReq, MainRes](implicit findId1: FindChildId[MainReq, Req1], findId2: FindChildId[MainReq, Req2], merge: Merge[Res1, Res2, MainRes], ex: ExecutionContext): Kleisli[MainReq, MainRes] =
      new MergerKleisli[MainReq, MainRes, Req1, Res1, Req2, Res2](tuple._1, tuple._2)
  }

}

trait Merge[Res1, Res2, MainRes] extends ((Res1, Res2) => MainRes)

trait FindChildId[Parent, ChildReq] extends (Parent => ChildReq)