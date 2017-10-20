package org.validoc.utilities

import utilities.kleisli.Kleisli

import scala.collection.concurrent.TrieMap
import scala.collection.mutable
import scala.concurrent.Future
import scala.reflect.ClassTag

trait ServiceType

trait Tree {
  type RawReq
  type RawRes

  def reqClassTag: ClassTag[RawReq]

  def resClassTag: ClassTag[RawRes]

  def service: Kleisli[RawReq, RawRes]

  def children: Seq[Tree]


}

case class ServiceTree[Req, Res](service: Req => Future[Res], children: Seq[Tree])(implicit val reqClassTag: ClassTag[Req], val resClassTag: ClassTag[Res]) extends Tree {
  type RawReq = Req
  type RawRes = Res
}

trait ServiceTreeAdder[ST <: ServiceType] {
  def addRoot[Req: ClassTag, Res: ClassTag](service: Kleisli[Req, Res]): Kleisli[Req, Res]

  def addOneChild[Req: ClassTag, Res: ClassTag](service: Kleisli[Req, Res], child: Kleisli[_, _]): Kleisli[Req, Res]

  def addServices[Req: ClassTag, Res: ClassTag](service: Kleisli[Req, Res], children: Seq[Kleisli[_, _]]): Kleisli[Req, Res]

}

class MutableServiceTreesAdder[ST <: ServiceType : ClassTag](serviceTrees: MutableServiceTrees) extends ServiceTreeAdder[ST] {
  def addRoot[Req: ClassTag, Res: ClassTag](service: Kleisli[Req, Res]): Kleisli[Req, Res] = serviceTrees.addService[Req, Res, ST](service, Seq())

  def addOneChild[Req: ClassTag, Res: ClassTag](service: Kleisli[Req, Res], child: Kleisli[_, _]): Kleisli[Req, Res] = serviceTrees.addService[Req, Res, ST](service, Seq(child))

  def addServices[Req: ClassTag, Res: ClassTag](service: Kleisli[Req, Res], children: Seq[Kleisli[_, _]]): Kleisli[Req, Res] = serviceTrees.addService[Req, Res, ST](service, children)
}

class NoServiceTreeAdder[ST <: ServiceType] extends ServiceTreeAdder[ST] {
  override def addRoot[Req: ClassTag, Res: ClassTag](service: Kleisli[Req, Res]) = service

  override def addOneChild[Req: ClassTag, Res: ClassTag](service: Kleisli[Req, Res], child: Kleisli[_, _]) = service

  override def addServices[Req: ClassTag, Res: ClassTag](service: Kleisli[Req, Res], children: Seq[Kleisli[_, _]]) = service
}

trait ServiceTrees {
  def roots: Set[Tree]

  def treeForService(kleisli: Kleisli[_, _]): Option[Tree]

  def add[ST <: ServiceType : ClassTag]: ServiceTreeAdder[ST]

  def servicesWith[ST <: ServiceType : ClassTag, Req: ClassTag, Res: ClassTag]: List[Kleisli[Req, Res]]
}


object ServiceTrees {

  implicit object NoServiceTrees extends ServiceTrees {
    override def roots = Set()


    override def treeForService(kleisli: Kleisli[_, _]) = None

    override def add[ST <: ServiceType : ClassTag] = new NoServiceTreeAdder[ST]

    override def servicesWith[ST <: ServiceType : ClassTag, Req: ClassTag, Res: ClassTag] = List()
  }

}

class ServiceNotRegisteredException(kleisli: Kleisli[_, _]) extends Exception(kleisli.toString())

class MutableServiceTrees extends ServiceTrees {
  private var serviceToTree = Map[Kleisli[_, _], Tree]()
  private var classToListOfServices = TrieMap[Class[_], mutable.MutableList[Tree]]()

  var roots = Set[Tree]()

  def addService[Req: ClassTag, Res: ClassTag, ST <: ServiceType](service: Kleisli[Req, Res], children: Seq[Kleisli[_, _]])(implicit stClassTag: ClassTag[ST]) = {
    val childTrees = children.map(c => serviceToTree.getOrElse(c, throw new ServiceNotRegisteredException(c)))
    val tree = ServiceTree(service, childTrees)
    roots = (roots -- childTrees) + tree
    serviceToTree = serviceToTree + (service -> tree)
    val list = classToListOfServices.getOrElseUpdate(stClassTag.runtimeClass, new mutable.MutableList())
    list += tree
    service
  }

  override def treeForService(kleisli: Kleisli[_, _]) = serviceToTree.get(kleisli)


  override def add[ST <: ServiceType : ClassTag] = new MutableServiceTreesAdder[ST](this)

  def clazz[T: ClassTag] = implicitly[ClassTag[T]].runtimeClass

  override def servicesWith[ST <: ServiceType : ClassTag, Req: ClassTag, Res: ClassTag] = {
    val list = classToListOfServices.get(clazz[ST]).getOrElse(mutable.MutableList[Tree]())
    list.filter(tree => tree.reqClassTag.runtimeClass == clazz[Req] && tree.resClassTag == clazz[Req]).map(_.service.asInstanceOf[Kleisli[Req, Res]]).toList
  }
}

