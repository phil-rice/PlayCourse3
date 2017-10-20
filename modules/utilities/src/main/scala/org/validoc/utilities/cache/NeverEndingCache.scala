package org.validoc.utilities.cache

import org.validoc.utilities.{ServiceTrees, ServiceType}
import org.validoc.utilities.kleisli.KleisliDelegate
import utilities.kleisli.Kleisli

import scala.collection.concurrent.TrieMap
import scala.concurrent.Future
import scala.reflect.ClassTag

case object Cache extends ServiceType

class NeverEndingCache[Req, Res](delegate: Kleisli[Req, Res]) extends Kleisli[Req, Res] {
  val trieMap = TrieMap[Req, Future[Res]]()

  override def apply(req: Req) = trieMap.getOrElseUpdate(req, delegate(req))

}

trait CacheLanguage {

  def cache[Req: ClassTag, Res: ClassTag](implicit serviceTrees: ServiceTrees) = new KleisliDelegate[Req, Res] {
    override def apply(delegate: Kleisli[Req, Res]) = serviceTrees.addOneChild(Cache, new NeverEndingCache(delegate), delegate)
  }
}