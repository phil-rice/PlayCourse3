package utilities

import scala.concurrent.{ExecutionContext, Future}

object Arrows {

  implicit class AnyPimper[T](t: T) {
    def ~>[T1](fn: T => T1) = fn(t)
  }

  implicit class FuturePimper[T](t: Future[T])(implicit ex: ExecutionContext) {
    def ~>[T1](fn: T => T1) = t.map(fn)
    def ~~>[T1](fn: T => Future[T1]): Future[T1] = t.flatMap(fn)
  }

  implicit class SeqPimper[T](list: Seq[T]){
    def ~>[T1](fn: T => T1) = list.map(fn)
  }
//  implicit class SeqFuturePimper[T](f: Future[Seq[T]])(implicit ex: ExecutionContext) {
//    def ~>[T1](fn: T => T1): Future[Seq[T1]] = f.map(_.map(fn))
//
////    def ~~>[T1](fn: T => Future[T1]): Future[Seq[T1]] = f.flatMap(list => Future.sequence(list.map(fn)))
//  }

  implicit class FutureSeqPimper[T](list: Seq[Future[T]])(implicit ex: ExecutionContext){
    def ~~>[T1](fn: Seq[T] => T1): Future[T1] = Future.sequence(list).map(fn)
  }


}
