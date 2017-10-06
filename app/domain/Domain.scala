package domain

trait HomePageQuery

object HomePageQuery extends HomePageQuery

case class HomePage(mostPopular: EnrichedMostPopular, promotions: EnrichedPromotion)

trait MostPopularQuery

object MostPopularQuery extends MostPopularQuery

case class MostPopular(programmeIds: Seq[ProgrammeId])

case class EnrichedMostPopular(programmes: Seq[Programme])

case class ProductionId(id: String) extends AnyVal

case class Production(id: ProductionId, info: String)

case class ProgrammeId(id: String) extends AnyVal

case class Programme(id: ProgrammeId, info: String)

trait PromotionQuery

object PromotionQuery extends PromotionQuery

case class Promotion(productionIds: Seq[ProductionId])

case class EnrichedPromotion(productions: Seq[Production])
