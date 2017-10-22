package org.validoc.utilities

import utilities.kleisli.Kleisli

class ServiceTreesSpec extends UnitSpec {

  trait RootServiceType extends ServiceType

  behavior of "ServiceTrees"

  it should "allow roots to be added" in {
    val serviceTrees = new MutableServiceTrees
    val service1 = stub[Kleisli[String, Int]]
    val service2 = stub[Kleisli[String, Int]]
    serviceTrees.addRoots[RootServiceType, String, Int](service1, service2)
    val Some(tree1) = serviceTrees.treeForService(service1)
    val Some(tree2) = serviceTrees.treeForService(service2)
    tree1.service shouldBe service1
    tree2.service shouldBe service2

    serviceTrees.servicesWith[RootServiceType, String, Int] shouldBe List((None, service1), (None, service2))
  }

}
