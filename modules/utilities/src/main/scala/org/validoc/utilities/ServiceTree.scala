package org.validoc.utilities

import utilities.kleisli.Kleisli

import scala.collection.concurrent.TrieMap
import scala.collection.mutable
import scala.concurrent.Future
import scala.reflect.ClassTag
import scala.xml.{EntityRef, NodeSeq, Text, XML}

trait ServiceType

trait Tree {
  type RawReq
  type RawRes
  type RawST

  def st: Option[RawST]

  def stClass: Class[RawST]

  implicit def reqClassTag: ClassTag[RawReq]

  implicit def resClassTag: ClassTag[RawRes]

  def service: Kleisli[RawReq, RawRes]

  def stDisplayString: DisplayString[RawST]

  def children: Seq[Tree]
}

object Tree {

  implicit object TreeAsHtml extends AsHtml[Tree] {
    override def apply(tree: Tree) = {
      import tree._
      Text(st.fold(DisplayString.functionDisplayString[RawReq, RawRes](stClass.getSimpleName))(stDisplayString))
    }
  }

}

case class ServiceTree[ST <: ServiceType, Req, Res](st: Option[ST], service: Req => Future[Res], children: Seq[Tree])(implicit val reqClassTag: ClassTag[Req], val resClassTag: ClassTag[Res], val stClassTag: ClassTag[ST], val stDisplayString: DisplayString[ST]) extends Tree {
  type RawReq = Req
  type RawRes = Res
  type RawST = ST

  override def stClass = stClassTag.runtimeClass.asInstanceOf[Class[ST]]
}

object ServiceTree {
  implicit def DisplayStringsForServiceTree[ST <: ServiceType, Req, Res] = new DisplayString[ServiceTree[ST, Req, Res]] {
    override def apply(tree: ServiceTree[ST, Req, Res]) = {
      import tree._
      tree.st.fold(DisplayString.functionDisplayString[Req, Res](stClassTag.runtimeClass.getSimpleName))(stDisplayString)
    }
  }
}

trait ServiceTreeAdder[ST <: ServiceType] {
  def addRoot[Req: ClassTag, Res: ClassTag](service: Kleisli[Req, Res]): Kleisli[Req, Res]

  def addOneChild[Req: ClassTag, Res: ClassTag](service: Kleisli[Req, Res], child: Kleisli[_, _]): Kleisli[Req, Res]

  def addServices[Req: ClassTag, Res: ClassTag](service: Kleisli[Req, Res], children: Seq[Kleisli[_, _]]): Kleisli[Req, Res]
}

class MutableServiceTreesAdder[ST <: ServiceType : ClassTag : DisplayString](st: Option[ST], serviceTrees: MutableServiceTrees) extends ServiceTreeAdder[ST] {
  def addRoot[Req: ClassTag, Res: ClassTag](service: Kleisli[Req, Res]): Kleisli[Req, Res] = serviceTrees.addService[Req, Res, ST](st, service, Seq())

  def addOneChild[Req: ClassTag, Res: ClassTag](service: Kleisli[Req, Res], child: Kleisli[_, _]): Kleisli[Req, Res] = serviceTrees.addService[Req, Res, ST](st, service, Seq(child))

  def addServices[Req: ClassTag, Res: ClassTag](service: Kleisli[Req, Res], children: Seq[Kleisli[_, _]]): Kleisli[Req, Res] = serviceTrees.addService[Req, Res, ST](st, service, children)
}

class NoServiceTreeAdder[ST <: ServiceType] extends ServiceTreeAdder[ST] {
  override def addRoot[Req: ClassTag, Res: ClassTag](service: Kleisli[Req, Res]) = service

  override def addOneChild[Req: ClassTag, Res: ClassTag](service: Kleisli[Req, Res], child: Kleisli[_, _]) = service

  override def addServices[Req: ClassTag, Res: ClassTag](service: Kleisli[Req, Res], children: Seq[Kleisli[_, _]]) = service
}

trait ServiceTrees {
  def roots: Set[Tree]

  def treeForService(kleisli: Kleisli[_, _]): Option[Tree]

  def add[ST <: ServiceType : ClassTag : DisplayString]: ServiceTreeAdder[ST]

  def add[ST <: ServiceType : ClassTag : DisplayString](st: ST): ServiceTreeAdder[ST]

  def servicesWith[ST <: ServiceType : ClassTag, Req: ClassTag, Res: ClassTag]: List[(Option[ST], Kleisli[Req, Res])]

  def servicesWithSome[ST <: ServiceType : ClassTag, Req: ClassTag, Res: ClassTag]: List[(ST, Kleisli[Req, Res])] = servicesWith[ST, Req, Res].collect({ case (Some(st), tree) => (st, tree) })

  def toMap[ST <: ServiceType : ClassTag, Req: ClassTag, Res: ClassTag](fn: ST => String): Map[String, Kleisli[Req, Res]] = servicesWithSome[ST, Req, Res].foldLeft(Map[String, Kleisli[Req, Res]]()) { case (acc, (st, k)) => acc + (fn(st) -> k) }
}


object ServiceTrees {

  implicit object NoServiceTrees extends ServiceTrees {
    override def roots = Set()

    override def treeForService(kleisli: Kleisli[_, _]) = None

    override def add[ST <: ServiceType : ClassTag : DisplayString] = new NoServiceTreeAdder[ST]

    override def servicesWith[ST <: ServiceType : ClassTag, Req: ClassTag, Res: ClassTag] = List()

    override def add[ST <: ServiceType : ClassTag : DisplayString](st: ST) = new NoServiceTreeAdder[ST]
  }

}

class ServiceNotRegisteredException(st: Option[_], kleisli: Kleisli[_, _]) extends Exception(st + "/" + kleisli.toString())

class MutableServiceTrees extends ServiceTrees {
  def addRoots[ST <: ServiceType : ClassTag : DisplayString, Req: ClassTag, Res: ClassTag](kleislis: Kleisli[Req, Res]*) = kleislis.foreach(add[ST].addRoot)

  private var serviceToTree = Map[Kleisli[_, _], Tree]()
  private var classToListOfServices = TrieMap[Class[_], mutable.MutableList[Tree]]()

  var roots = Set[Tree]()

  def addService[Req: ClassTag, Res: ClassTag, ST <: ServiceType : DisplayString](st: Option[ST], service: Kleisli[Req, Res], children: Seq[Kleisli[_, _]])(implicit stClassTag: ClassTag[ST]) = try {
    val childTrees = children.map(c => serviceToTree.getOrElse(c, throw new ServiceNotRegisteredException(st, c)))
    val tree = ServiceTree(st, service, childTrees)
    roots = (roots -- childTrees) + tree
    serviceToTree = serviceToTree + (service -> tree)
    val list = classToListOfServices.getOrElseUpdate(stClassTag.runtimeClass, new mutable.MutableList())
    list += tree
    service
  } catch {
    case e: Exception => e.printStackTrace; throw e
  }

  override def treeForService(kleisli: Kleisli[_, _]) = serviceToTree.get(kleisli)

  override def add[ST <: ServiceType : ClassTag : DisplayString] = new MutableServiceTreesAdder[ST](None, this)

  override def add[ST <: ServiceType : ClassTag : DisplayString](st: ST) = new MutableServiceTreesAdder[ST](Some(st), this)

  def clazz[T: ClassTag] = implicitly[ClassTag[T]].runtimeClass

  override def servicesWith[ST <: ServiceType : ClassTag, Req: ClassTag, Res: ClassTag]: List[(Option[ST], Kleisli[Req, Res])] = {
    val list = classToListOfServices.get(clazz[ST]).getOrElse(mutable.MutableList()).toList
    list.collect { case tree if tree.reqClassTag.runtimeClass == clazz[Req] && tree.resClassTag.runtimeClass == clazz[Res] && tree.st.fold(true)(x => x.getClass == clazz[ST]) => (tree.st.map(_.asInstanceOf[ST]), tree.service.asInstanceOf[Kleisli[Req, Res]]) }
  }
}

object IndentAnd {
  implicit def defaultAsHtml[T](implicit asHtmlForT: AsHtml[T]): AsHtml[IndentAnd[T]] = new AsHtml[IndentAnd[T]] {
    override def apply(v1: IndentAnd[T]) = {
      println(s"In indent and ${v1.indent}: ${List.fill(v1.indent)(Text("&nbsp;")) ::: asHtmlForT(v1.t).toList}")
      NodeSeq.fromSeq(List.fill(v1.indent * 4)(EntityRef("nbsp")) ::: asHtmlForT(v1.t).toList)
    }
  }
}

case class IndentAnd[T](indent: Int, t: T)

class ServiceTreeAsMap(serviceTrees: ServiceTrees) {
  def asString(serviceTree: Tree) = serviceTree.st.fold(serviceTree.stClass.getSimpleName)(st => st.toString)

  def treeAsList = serviceTrees.roots.toList.sortBy(st => st.stClass.getSimpleName + "" + st.st).flatMap(asList(_, 0))

  def asList(serviceTree: Tree, depth: Int = 0): List[IndentAnd[Tree]] = List(IndentAnd(depth, serviceTree)) ++ serviceTree.children.flatMap(c => asList(c, depth + 1))
}