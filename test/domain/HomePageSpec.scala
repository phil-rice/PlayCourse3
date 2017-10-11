package domain

class HomePageSpec extends UnitSpec with MergerFixture[HomePageQuery, HomePage, EnrichedPromotion, EnrichedMostPopular] {

  behavior of "HomePageQuery"

  it should "have a FindChildId[HomePageQuery,MostPopularQuery]" in {
    setupFindChildId[MostPopularQuery](findChildId => findChildId(HomePageQuery) shouldBe MostPopularQuery)
  }

  it should "have a FindChildId[HomePageQuery,PromotionQuery]" in {
    setupFindChildId[PromotionQuery](findChildId => findChildId(HomePageQuery) shouldBe PromotionQuery)
  }

  behavior of "HomePage"

  it should "have a merger that will merge enriched promotions and enriched most popular making home pages" in {
    val enrichedMostPopular = mock[EnrichedMostPopular]
    val enrichedPromotion = mock[EnrichedPromotion]

    setupMerger { (merger) =>
      merger(enrichedPromotion, enrichedMostPopular) shouldBe HomePage(enrichedMostPopular, enrichedPromotion)
    }
  }


}
