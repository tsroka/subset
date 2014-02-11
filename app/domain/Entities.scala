package domain

import org.joda.time.DateTime
import java.util.Date
import play.api.libs.json.{Writes, Reads, Json}
import java.io.ByteArrayOutputStream
import scala.io.Source
import java.sql.Blob
import scalax.io.Resource

case class RuleSpecification(sideField: String, side1Value: String, groupFields: Array[String], matchFields: Array[String])

case class Rule(id: Long, name: String, ruleJson: RuleSpecification, created: DateTime)

case class CsvData(name: String, content: CsvSpreadSheet, ruleId: Long, created: DateTime)


case class CsvSpreadSheet(name: String, headers: List[String], rows: List[List[String]]) {
  val colsNum = rows.length
  val rowsNum = headers.length

  def headerAt(col: Int): String = headers(col)

  def valueAt(col: Int, row: Int): String = rows(row)(col)
}


object CsvSpreadSheet {
  val DELIMITER = ','

  /**
   * Parses csv file
   * @param name
   * @param content
   * @return either representation of CsvSpreadSheet or error message
   */
  def fromBinaryArray(name: String, content: Array[Byte]): Either[CsvSpreadSheet, String] = {
    val lines: Iterator[String] = Source.fromBytes(content).getLines()
    if (!lines.hasNext)
      Right("No content.")

    val headers: List[String] = lines.next().split(DELIMITER).toList
    parseRows(lines, headers.length).left.map(rows => CsvSpreadSheet(name, headers, rows))
  }

  private def parseRows(lines: Iterator[String], noOfCols: Integer): Either[List[List[String]], String] = {
    var rows: List[List[String]] = List[List[String]]()
    var rowNum = 1
    while (lines.hasNext) {
      val row: List[String] = lines.next().split(DELIMITER).toList
      if (row.length != noOfCols) {
        return Right(s"Invalid CSV file - no of columns in line ${rowNum} should be ${noOfCols}.")
      }
      rows = rows :+ row
      rowNum += 1
    }
    Left(rows)
  }

  def asArrayBytes(csv: CsvSpreadSheet): Array[Byte] = {
    val bout = new ByteArrayOutputStream()
    val header = csv.headers.mkString(DELIMITER.toString) + "\n"
    bout.write(header.getBytes)
    bout.write(csv.rows.map(_.mkString(DELIMITER.toString)).mkString("\n").getBytes)
    bout.toByteArray
  }
}


object EntitiesDao {

  import play.api.Play.current
  import play.api.db.DB
  import anorm._

  implicit val ruleSpecificationReads = Json.reads[RuleSpecification]
  implicit val ruleSpecificationWrites = Json.writes[RuleSpecification]

  implicit val ruleReads = Json.reads[Rule]
  implicit val ruleWrites = Json.writes[Rule]

  implicit val csvSpreadSheetReads = Json.reads[CsvSpreadSheet]
  implicit val csvSpreadSheetWrites = Json.writes[CsvSpreadSheet]

  implicit val csvDataSheetReads = Json.reads[CsvData]
  implicit val csvDataSheetWrites = Json.writes[CsvData]


  val SELECT_RULE_SQL = "SELECT id, name, ruleJson, created FROM Rule"
  val INSERT_RULE_SQL = "INSERT INTO RULE(name, ruleJson, created) VALUES ({name},{ruleJson},{created})"
  val RULE_BY_ID = s"${SELECT_RULE_SQL} WHERE id = {id}"

  val SELECT_CSV_DATA_SQL = "SELECT name, content, ruleId, created FROM CsvData"
  val INSERT_CSV_DATA_SQL = "INSERT INTO CsvData(name, content, ruleId, created) VALUES ({name},{content},{ruleId},{created})"
  val UPDATE_CSV_DATA_SQL = "UPDATE CsvData SET content = {content}, ruleId = {ruleId} WHERE name = {name}"
  val CSV_DATA_BY_NAME = s"${SELECT_CSV_DATA_SQL} WHERE name = {name}"

  def jsonToObj[T](jsonBin: Array[Byte])(implicit fjs: Reads[T]): T = {
    Json.fromJson[T](Json.parse(jsonBin)).fold[T](
      errors => throw new RuntimeException(errors.mkString("\n")),
      value => value
    )
  }

  def objToJson[T](obj: T)(implicit fjs: Writes[T]): Array[Byte] = {
    Json.toJson(obj).toString().getBytes()
  }

  private implicit def blobToArray(b: Blob): Array[Byte] = Resource.fromInputStream(b.getBinaryStream).byteArray

  private val ruleMapper: PartialFunction[Any, Rule] = {
    case Row(id: Long, name: String, ruleJson: Blob, created: Date) =>
      val ruleSpecs: RuleSpecification = jsonToObj[RuleSpecification](ruleJson)
      Rule(id, name, ruleSpecs, new DateTime(created.getTime))
    case _ => throw new RuntimeException("Unable to map row")
  }

  private val csvDataMapper: PartialFunction[Any, CsvData] = {
    case Row(name: String, content: Blob, ruleId: Long, created: Date) =>
      val spreadSheet = jsonToObj[CsvSpreadSheet](content)
      CsvData(name, spreadSheet, ruleId, new DateTime(created.getTime))
    case _ => throw new RuntimeException("Unable to map row")
  }

  def getRuleById(id: Long): Option[Rule] = DB.withConnection {
    implicit c =>
      SQL(RULE_BY_ID).on("id" -> id)().map(ruleMapper).headOption
  }

  def getCsvByName(name: String): Option[CsvData] = DB.withConnection {
    implicit c =>
      SQL(CSV_DATA_BY_NAME).on("name" -> name)().map(csvDataMapper).headOption
  }

  def getRules(): List[Rule] = DB.withConnection {
    implicit c =>
      SQL(SELECT_RULE_SQL)().map(ruleMapper).toList
  }

  def insertRule(r: Rule): Option[Long] = DB.withConnection {
    implicit c =>
      SQL(INSERT_RULE_SQL).on("name" -> r.name, "ruleJson" -> objToJson(r.ruleJson), "created" -> new Date()).executeInsert[Option[Long]]()
  }


  def insertCsvData(data: CsvData) = DB.withConnection {
    implicit c =>
      SQL(INSERT_CSV_DATA_SQL).on("name" -> data.name, "content" -> objToJson(data.content), "ruleId" -> data.ruleId,
        "created" -> new Date()).executeInsert()
  }

  def updateCsvData(data: CsvData) = DB.withConnection {
    implicit c =>
      SQL(UPDATE_CSV_DATA_SQL).on("name" -> data.name, "content" -> objToJson(data.content), "ruleId" -> data.ruleId).executeUpdate()
  }

}
