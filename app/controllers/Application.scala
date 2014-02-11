package controllers

import play.api._
import play.api.mvc._
import play.api.libs.Files.TemporaryFile
import java.nio.file.Files
import domain._
import domain.EntitiesDao._
import play.api.libs.json.{JsError, Json}
import play.api.libs.json.JsSuccess
import domain.RuleSpecification
import domain.Rule
import play.api.mvc.MultipartFormData.FilePart
import org.joda.time.DateTime

object Application extends Controller {

  def index = Action {
    Ok(views.html.index())
  }


}