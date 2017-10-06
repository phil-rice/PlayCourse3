package utilities.cache

import utilities.kleisli.{Kleisli, KleisliDelegate}

import scala.collection.concurrent.TrieMap
import scala.concurrent.Future

class NeverEndingCache[Req, Res](delegate: Kleisli[Req, Res]) extends Kleisli[Req, Res] {
  val trieMap = TrieMap[Req, Future[Res]]()

  override def apply(req: Req) = trieMap.getOrElseUpdate(req, delegate(req))

}

trait CacheLanguage {
  def cache[Req, Res] = new KleisliDelegate[Req, Res] {
    override def apply(delegate: Kleisli[Req, Res]) = new NeverEndingCache(delegate)
  }
}