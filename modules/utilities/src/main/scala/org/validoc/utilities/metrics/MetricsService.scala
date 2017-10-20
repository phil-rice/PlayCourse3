package org.validoc.utilities.metrics

import org.validoc.utilities.Arrows._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try
import org.validoc.utilities.kleisli.Kleislis._
import org.validoc.utilities.Arrows._
import org.validoc.utilities.{ServiceTrees, ServiceType}
import org.validoc.utilities.kleisli.KleisliDelegate
import utilities.kleisli.Kleisli

import scala.reflect.ClassTag

trait MetricNameFor[Res] extends (String => Try[Res] => String)

trait IncrementMetric extends (String => Unit) {
  def value: Long
}

trait MetricsServiceType extends ServiceType

trait MetricsLanguage {
  def metric[Req: ClassTag, Res: ClassTag](metricRootName: String)(delegate: Kleisli[Req, Res])
                                          (implicit metricNameFor: MetricNameFor[Res], incrementMetric: IncrementMetric, serviceTrees: ServiceTrees, ex: ExecutionContext): KleisliDelegate[Req, Res] = new KleisliDelegate[Req, Res] {
    override def apply(v1: Kleisli[Req, Res]) = serviceTrees.add[MetricsServiceType].addOneChild(delegate.sideeffect(metricNameFor(metricRootName) ~> incrementMetric), delegate)
  }
}