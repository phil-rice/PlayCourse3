package controllers

import javax.inject.{Inject, Singleton}

import domain._
import play.api.mvc.{AbstractController, ControllerComponents}
import services.Services

import scala.concurrent.{ExecutionContext, Future}


@Singleton
class InternalController @Inject()(cc: ControllerComponents, services: Services) extends AbstractController(cc) {
  implicit val ec = ExecutionContext.global

  def vogue = Action.async { implicit request =>
    Future.successful(Ok("you need to call the services.vogue method with a MostPopularQuery and then return the ToString of it, The url is 'vogue'/mostPopular"))
  }

  def billboard = Action.async { implicit request =>
    services.billboard(PromotionQuery).map { promotion => Ok(promotion.toString).as("text/html") }
  }

  def programme(id: String) = Action.async { implicit request =>
    services.programmeFnord(ProgrammeId(id)).map { promotion => Ok(promotion.toString).as("text/html") }
  }

  def production(id: String) = Action.async { implicit request =>
    services.productionFnord(ProductionId(id)).map { promotion => Ok(promotion.toString).as("text/html") }
  }

  def enrichedPromotion = Action.async(implicit request =>
    services.enrichedPromotion(PromotionQuery).map { promotion => Ok(promotion.toString).as("text/html") }
  )

  def enrichedMostPopular = Action.async { implicit request =>
    services.enrichedMostPopular(MostPopularQuery).map { mostPopular => Ok(mostPopular.toString).as("text/html") }
  }
  def homePage = Action.async { implicit request =>
    services.homePage(HomePageQuery).map { homePage => Ok("HomePage:" + homePage.mostPopular +"<br/>" + homePage.promotions).as("text/html") }
  }

}
