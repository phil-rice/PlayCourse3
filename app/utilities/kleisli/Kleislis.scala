package utilities.kleisli

import play.api.libs.ws.{WSRequest, WSResponse}
import utilities.cache.{CacheLanguage, NeverEndingCache}
import utilities.objectify.ObjectifyLanguage
import utilities.profile.ProfilingLanguage

import scala.concurrent.Future

trait Kleislis extends CacheLanguage with ProfilingLanguage with ObjectifyLanguage with EnricherLanguage with MergerLanguage{
  def http: Kleisli[WSRequest, WSResponse] = { httpRequest: WSRequest => httpRequest.execute() }

}

object Kleislis extends Kleislis {

  implicit class KleisliPimper[Req, Res](k:  Req => Future[Res]){
    def |+|[Req2, Res2](tr: KleisliTransformer[Req, Res, Req2, Res2])= tr(k)
    def andDoIt[Req2, Res2](tr: KleisliTransformer[Req, Res, Req2, Res2])= tr(k)
    def andDoThis = ???
  }


}
