package controllers

import play.api.mvc._

object SubsetController extends Controller {

  def list = Action {
    Ok("")
  }

  def run(csvName: String, ruleId: Option[Long]) = Action {
    request =>
      Ok("")
  }


}