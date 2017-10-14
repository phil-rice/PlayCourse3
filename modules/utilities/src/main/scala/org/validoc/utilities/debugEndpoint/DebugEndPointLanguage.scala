package org.validoc.utilities.debugEndpoint

import org.validoc.utilities.kleisli.KleisliTransformer
import utilities.kleisli.Kleisli

import scala.concurrent.{ExecutionContext, Future}

trait MakeDebugQuery[Req] extends (String => Req)

trait DebugToString[Res] extends (Res => String)

object DebugToString {
  implicit def default[T] = new DebugToString[T] {
    override def apply(v1: T) = v1.toString
  }
}


trait DebugEndPointLanguage {

  import org.validoc.utilities.Arrows._
  import org.validoc.utilities.kleisli.Kleislis._


  def debug[Req, Res](implicit makeQuery: MakeDebugQuery[Req], debugToString: DebugToString[Res], ex: ExecutionContext): KleisliTransformer[Req, Res, String, String] =
    new KleisliTransformer[Req, Res, String, String] {
      override def apply(kleisli: Kleisli[Req, Res]): Kleisli[String, String] = makeQuery ~> kleisli ~> debugToString
    }

  class DebugEndpoint[Req, Res](name: String, kleisli: Kleisli[Req, Res])(implicit makeQuery: MakeDebugQuery[Req], debugToString: DebugToString[Res], executionContext: ExecutionContext) extends (String => Future[String]) {
    override def apply(request: String) = kleisli(makeQuery(request)).map(debugToString)
  }

}