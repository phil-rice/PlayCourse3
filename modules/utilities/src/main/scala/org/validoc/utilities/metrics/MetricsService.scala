package org.validoc.utilities.metrics

import org.validoc.utilities.Arrows._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try
import org.validoc.utilities.kleisli.Kleislis._
import org.validoc.utilities.Arrows._
import org.validoc.utilities.kleisli.KleisliDelegate
import utilities.kleisli.Kleisli

trait MetricNameFor[Res] extends (String => Try[Res] => String)

trait IncrementMetric extends (String => Unit)

trait MetricsLanguage {
  def metric[Req, Res](metricRootName: String)(delegate: Req => Future[Res])
                      (implicit metricNameFor: MetricNameFor[Res], incrementMetric: IncrementMetric, ex: ExecutionContext): KleisliDelegate[Req, Res] = new KleisliDelegate[Req, Res] {
    override def apply(v1: Kleisli[Req, Res]) = delegate.sideeffect(metricNameFor(metricRootName) ~> incrementMetric)
  }
}