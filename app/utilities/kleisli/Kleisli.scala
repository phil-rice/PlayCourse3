package utilities.kleisli

import play.api.libs.ws.{WSRequest, WSResponse}

trait KleisliTransformer[Req1, Res1, Req2, Res2] extends (Kleisli[Req1, Res1] => Kleisli[Req2, Res2])

trait KleisliDelegate[Req, Res] extends KleisliTransformer[Req, Res, Req, Res]

object KleisliTransformer {


}