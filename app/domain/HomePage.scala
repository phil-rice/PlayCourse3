package domain

import utilities.kleisli.{FindChildId, Merge}

trait HomePageQuery

object HomePageQuery extends HomePageQuery {

}

case class HomePage(mostPopular: EnrichedMostPopular, promotions: EnrichedPromotion)

object HomePage {

}