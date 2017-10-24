package org.validoc.utilities

import java.util.UUID

case class TraceId(id: String) extends AnyVal


trait TraceIdGenerator extends (() => TraceId) {
  def getOrCreate[T](t: T)(implicit hasTraceId: HasTraceId[T]) = hasTraceId(t).getOrElse(apply())
}


trait HasTraceId[T] extends (T => Option[TraceId])

object TraceIdGenerator {


  implicit object DefaultTraceIdGenerator extends TraceIdGenerator {
    override def apply() = TraceId(UUID.randomUUID().toString)
  }

}
