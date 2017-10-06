package utilities

import scala.concurrent.Future

package object kleisli {

  type Kleisli[Req, Res] = Req => Future[Res]

}
