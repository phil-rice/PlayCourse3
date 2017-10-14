package utilities

import scala.concurrent.{ExecutionContext, Future}

object Arrows {

  implicit class FunctionPimper[Req, Res](fn: Req => Res){
    def ~>[Res2](fn2: Res => Res2) = fn andThen fn2
  }
  implicit class FnMakesSeqPimper[Req, Res](fn: Req => Seq[Res]) {
    def |+|[T](mapfn: Res => T) = { r: Req => fn(r).map(mapfn) }
  }

  implicit class FnMakesSeqFuturePimper[Req, Res](fn: Req => Seq[Future[Res]])(implicit ex: ExecutionContext) {
    def toFuture: (Req) => Future[Seq[Res]] = { req: Req => Future.sequence(fn(req)) }
  }

  implicit class AnyPimper[T](t: T) {
    def ~>[T1](fn: T => T1) = fn(t)
  }

  implicit class FuturePimper[T](futT: Future[T])(implicit ex: ExecutionContext) {
    def ~~>[T1](fn: T => T1) = futT.map(fn)

    def ~~~>[T1](fn: T => Future[T1]): Future[T1] = futT.flatMap(fn)
  }

  implicit class SeqPimper[T](list: Seq[T]) {
    def ~>[T1](fn: T => T1) = list.map(fn)
  }

  implicit class FutureSeqPimper[T](list: Seq[Future[T]])(implicit ex: ExecutionContext) {
    def ~~>[T1](fn: Seq[T] => T1): Future[T1] = Future.sequence(list).map(fn)
  }

}
