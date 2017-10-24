package domain

import org.validoc.utilities.{MergerFixture, TraceId, UnitSpec}

class HomePageSpec extends UnitSpec with MergerFixture[HomePageQuery, HomePage, EnrichedPromotion, EnrichedMostPopular] {

  behavior of "HomePageQuery"

  val homePageQueryByPassCache = HomePageQuery(TraceId("someTraceId"), true)
  it should "have a FindChildId[HomePageQuery,MostPopularQuery]" in {
    setupFindChildId[MostPopularQuery](findChildId => findChildId(homePageQueryByPassCache) shouldBe MostPopularQuery(TraceId("someTraceId")))
  }

  it should "have a FindChildId[HomePageQuery,PromotionQuery]" in {
    setupFindChildId[PromotionQuery](findChildId => findChildId(homePageQueryByPassCache) shouldBe PromotionQuery)
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
