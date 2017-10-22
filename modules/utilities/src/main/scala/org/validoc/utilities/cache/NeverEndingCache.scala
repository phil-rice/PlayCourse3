package org.validoc.utilities.cache

import org.validoc.utilities.{AsHtml, DisplayString, ServiceTrees, ServiceType}
import org.validoc.utilities.kleisli.KleisliDelegate
import utilities.kleisli.Kleisli

import scala.collection.concurrent.TrieMap
import scala.concurrent.Future
import scala.reflect.ClassTag
import scala.xml.Text

case class Cache[Req: ClassTag, Res: ClassTag](actualCache: TrieMap[Req, Future[Res]]) extends ServiceType

object Cache {
  implicit def displayAsHtmlForCache[Req: ClassTag, Res: ClassTag] = new DisplayString[Cache[Req, Res]] {
    override def apply(v1: Cache[Req, Res]) = DisplayString.functionDisplayStringForClass[Cache[Req, Res], Req, Res]("")
  }
}

class NeverEndingCache[Req, Res](delegate: Kleisli[Req, Res]) extends Kleisli[Req, Res] {
  val trieMap = TrieMap[Req, Future[Res]]()

  override def apply(req: Req) = trieMap.getOrElseUpdate(req, delegate(req))
}

trait CacheLanguage {

  def cache[Req: ClassTag, Res: ClassTag](implicit serviceTrees: ServiceTrees) = new KleisliDelegate[Req, Res] {
    override def apply(delegate: Kleisli[Req, Res]) = {
      val cache = new NeverEndingCache(delegate)
      serviceTrees.add(Cache(cache.trieMap)).addOneChild[Req, Res](cache, delegate)
    }
  }
}