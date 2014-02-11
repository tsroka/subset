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

object CsvController extends Controller {

  def parseCsv = Action(parse.multipartFormData) {
    request =>
      request.body.file("csvFile").map {
        (csvFile: FilePart[TemporaryFile]) =>
          val filename = csvFile.filename
          CsvSpreadSheet.fromBinaryArray(filename, Files.readAllBytes(csvFile.ref.file.toPath))
            .fold(sheet => {
            Ok(Json.toJson(CsvData(filename, sheet,-1,new DateTime())))
          }, error => {
            NotAcceptable(Json.obj("error" -> error))
          })
      }.getOrElse {
        NotAcceptable(Json.obj("error" -> "No file in the form"))
      }
  }

  def uploadCsv(name: String) = Action(parse.json) {
    request =>
      Json.fromJson[CsvData](request.body) match {
        case JsSuccess(data, _) => {
          val exists = EntitiesDao.getCsvByName(name).isDefined
          if (!exists)
            EntitiesDao.insertCsvData(data.copy(name))
          else
            EntitiesDao.updateCsvData(data.copy(name))

          Ok(Json.toJson(EntitiesDao.getCsvByName(name)))
        }
        case JsError(errors) => NotAcceptable(Json.obj("error" -> errors.mkString("\n")))
      }
  }

}