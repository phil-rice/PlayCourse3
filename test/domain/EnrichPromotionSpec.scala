package domain

class EnrichPromotionSpec extends UnitSpec with EnricherFixture[PromotionQuery, Promotion, Production, EnrichedPromotion] {

  behavior of "EnrichMostPopular"

  val productionId1 = ProductionId("id1")
  val productionId2 = ProductionId("id2")
  val promotion = Promotion(List(productionId1, productionId1))

  val production1 = Production(productionId1, "someStuff1")
  val production2 = Production(productionId2, "someStuff2")
  val productions = List(production1, production2)

  it should " have an enricher " in {
    setupEnricher { enricher =>
      enricher.apply(PromotionQuery, promotion)(productions) shouldBe EnrichedPromotion(productions)
    }
  }
}
