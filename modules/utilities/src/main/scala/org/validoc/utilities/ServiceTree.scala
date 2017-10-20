package org.validoc.utilities

import utilities.kleisli.Kleisli

import scala.concurrent.Future
import scala.reflect.ClassTag

trait ServiceType

trait Tree {
  type RawReq
  type RawRes

  def serviceType: ServiceType

  def reqClassTag: ClassTag[RawReq]

  def resClassTag: ClassTag[RawRes]

  def service: Kleisli[RawReq, RawRes]

  def children: Seq[Tree]


}

case class ServiceTree[Req, Res](serviceType: ServiceType, service: Req => Future[Res], children: Seq[Tree])(implicit val reqClassTag: ClassTag[Req], val resClassTag: ClassTag[Res]) extends Tree {
  type RawReq = Req
  type RawRes = Res
}


trait ServiceTrees {
  def roots: Set[Tree]

  def treeForService(kleisli: Kleisli[_, _]): Option[(Tree, ServiceType)]

  def treesForServiceType(serviceType: ServiceType): Seq[Tree]


  def addService[Req: ClassTag, Res: ClassTag](serviceType: ServiceType, service: Kleisli[Req, Res], children: Seq[Kleisli[_, _]]): Kleisli[Req, Res]


  def addRoot[Req: ClassTag, Res: ClassTag](serviceType: ServiceType, service: Kleisli[Req, Res]): Kleisli[Req, Res] = addService(serviceType, service, Seq())

  def addOneChild[Req: ClassTag, Res: ClassTag](serviceType: ServiceType, service: Kleisli[Req, Res], child: Kleisli[_, _]): Kleisli[Req, Res] = addService(serviceType, service, Seq(child))

}

object ServiceTrees {

  implicit object NoServiceTrees extends ServiceTrees {
    override def roots = Set()

    override def addService[Req: ClassTag, Res: ClassTag](serviceType: ServiceType, service: Kleisli[Req, Res], children: Seq[Kleisli[_, _]]) = service

    override def treeForService(kleisli: Kleisli[_, _]) = None

    override def treesForServiceType(serviceType: ServiceType) = List()
  }

}

class MutableServiceTrees extends ServiceTrees {
  private var serviceToTree = Map[Kleisli[_, _], (Tree, ServiceType)]()
  var roots = Set[Tree]()

  def addService[Req: ClassTag, Res: ClassTag](serviceType: ServiceType, service: Kleisli[Req, Res], children: Seq[Kleisli[_, _]]) = {
    val childTrees = children.map(serviceToTree).map(_._1)
    val tree = ServiceTree(serviceType, service, childTrees)
    roots = (roots -- childTrees) + tree
    serviceToTree = serviceToTree + (service -> (tree, serviceType))
    service
  }

  override def treeForService(kleisli: Kleisli[_, _]) = serviceToTree.get(kleisli)

  override def treesForServiceType(serviceType: ServiceType) = serviceToTree.values.collect { case (tree, st) if st == serviceType => tree }.toList
}

