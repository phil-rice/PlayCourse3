package org.validoc.utilities.cache

import com.github.blemale.scaffeine.{AsyncLoadingCache, Scaffeine}
import org.validoc.utilities.kleisli.KleisliDelegate
import org.validoc.utilities.{DisplayString, ServiceTrees, ServiceType}
import utilities.kleisli.Kleisli

import scala.collection.concurrent.TrieMap
import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag
import org.validoc.utilities.Arrows._

case class Cache[Req: ClassTag, Res: ClassTag](actualCache: AsyncLoadingCache[Req, Res]) extends ServiceType

object Cache {
  implicit def displayAsHtmlForCache[Req: ClassTag, Res: ClassTag] = new DisplayString[Cache[Req, Res]] {
    override def apply(v1: Cache[Req, Res]) = DisplayString.functionDisplayStringForClass[Cache[Req, Res], Req, Res]("")
  }
}

trait FindCacheId[T] extends (T => Any)

object FindCacheId {
  implicit def default[T] = new FindCacheId[T] {
    override def apply(v1: T) = v1
  }
}

trait ByPassCache[T] extends (T => Boolean)


object ByPassCache {
  implicit def default[T] = new ByPassCache[T] {
    override def apply(v1: T) = false
  }
}

trait CacheLanguage {

  def cache[Req: ClassTag, Res: ClassTag](cacheSetup: Scaffeine[Any, Any])(implicit byPassCache: ByPassCache[Req], findCacheId: FindCacheId[Req], serviceTrees: ServiceTrees, ec: ExecutionContext) = new KleisliDelegate[Req, Res] {
    val cache: AsyncLoadingCache[Any, Res] = cacheSetup.buildAsyncFuture(_ => throw new RuntimeException("should never be executed"))

    override def apply(delegate: Kleisli[Req, Res]) = {
      val cacheService: Kleisli[Req, Res] = { req: Req => if (byPassCache(req)) delegate(req) else cache.getFuture(findCacheId(req), _ => delegate(req)) }
      serviceTrees.add(Cache(cache)).addOneChild[Req, Res](cacheService, delegate)
    }
  }
}