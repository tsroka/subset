package controllers

import play.api.mvc._
import domain._
import domain.EntitiesDao._
import play.api.libs.json.{JsError, Json}
import play.api.libs.json.JsSuccess
import domain.RuleSpecification
import domain.Rule

object RuleController extends Controller {

  def uploadRuleSpecification(name: String) = Action(parse.json) {
    request =>
      Json.fromJson[RuleSpecification](request.body) match {
        case JsSuccess(specification, _) => {
          val rule = Rule(-1, name, specification, null)
          EntitiesDao.insertRule(rule).map(id => Ok(Json.toJson(EntitiesDao.getRuleById(id))))
            .getOrElse(InternalServerError(Json.obj("error" -> "Unable to save ")))
        }
        case JsError(errors) => NotAcceptable(Json.obj("error" -> errors.mkString("\n")))
      }
  }

}