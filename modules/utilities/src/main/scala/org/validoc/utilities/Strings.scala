package org.validoc.utilities

import scala.reflect.ClassTag
import scala.xml._

trait AsHtml[T] extends (T => NodeSeq)

trait DisplayString[T] extends (T => String)

object DisplayString {
  implicit def default[T] = new DisplayString[T] {
    override def apply(v1: T) = v1.toString
  }

  def functionDisplayString[From, To](name: String)(implicit from: ClassTag[From], to: ClassTag[To]) =
    s"$name[${from.runtimeClass.getSimpleName}, ${to.runtimeClass.getSimpleName}]"

  def functionDisplayStringForClass[T, From, To](toRemoveIfExists: String)(implicit t: ClassTag[T], from: ClassTag[From], to: ClassTag[To]) =
    s"${Strings.removeFromEndIfExists(toRemoveIfExists)(t.runtimeClass.getSimpleName)}[${from.runtimeClass.getSimpleName}, ${to.runtimeClass.getSimpleName}]"

}

object AsHtml {

  implicit class AsHtmlPimper[T](t: T)(implicit asHtmlT: AsHtml[T]) {
    println(s"implicit is $asHtmlT")

    def asHtml = asHtmlT(t)
  }

  implicit def default[T](implicit displayString: DisplayString[T]) = new AsHtml[T] {
    override def apply(v1: T) = Text(displayString(v1))
  }
}

object Strings {

  def removeFromEndIfExists(toRemove: String)(s: String) = if (s.endsWith(toRemove)) s.substring(0, s.length - toRemove.length) else s
}
