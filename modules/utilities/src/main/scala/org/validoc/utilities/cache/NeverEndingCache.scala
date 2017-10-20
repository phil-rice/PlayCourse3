package org.validoc.utilities.cache

import org.validoc.utilities.kleisli.KleisliDelegate
import utilities.kleisli.Kleisli

import scala.collection.concurrent.TrieMap
import scala.concurrent.Future

trait Cache

class NeverEndingCache[Req, Res](delegate: Kleisli[Req, Res]) extends Kleisli[Req, Res] with Cache {
  val trieMap = TrieMap[Req, Future[Res]]()

  override def apply(req: Req) = trieMap.getOrElseUpdate(req, delegate(req))

}

trait CacheLanguage {

  var allCaches = List[Cache]()

  def addToCache[X](kleisli: X with Cache): X = {
    allCaches = allCaches :+ kleisli
    kleisli
  }


  def cache[Req, Res] = new KleisliDelegate[Req, Res] {
    override def apply(delegate: Kleisli[Req, Res]) = addToCache(new NeverEndingCache(delegate))
  }
}