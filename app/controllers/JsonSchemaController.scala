package controllers

import javax.inject._
import model._
import play.api._
import play.api.libs.json.JsValue
import play.api.mvc._
import repositories.JsonSchemaRepository

/**
 * Handles request dealing with Json schemas
 */
@Singleton
class JsonSchemaController @Inject()(val controllerComponents: ControllerComponents,
                                     val jsonSchemaRepository: JsonSchemaRepository)
  extends BaseController with Logging {

  val UPLOAD_ACTION = "uploadSchema"
  val DOWNLOAD_ACTION = "downloadSchema"

  /**
   * Store a new Json Schema
   */
  def uploadSchema(schemaId: String) = Action { implicit request: Request[AnyContent] =>
    request.body.asJson
      .map(_.toString)
      .orElse(request.body.asText) match {
      case None =>
        BadRequest(Error(UPLOAD_ACTION, schemaId, "Invalid content").toJson)
          .as(JSON)
      case Some(jsonString) =>
        jsonSchemaRepository.save(schemaId, jsonString)
        Created(Success(UPLOAD_ACTION, schemaId).toJson)
          .as(JSON)
    }
  }

  /**
   * Retrieve a Json Schema if it exists
   */
  def downloadSchema(schemaId: String) = Action { implicit request: Request[AnyContent] =>
    jsonSchemaRepository.read(schemaId) match {
      case Some(schema) => Ok(schema)
      case None =>
        BadRequest(Error(DOWNLOAD_ACTION, schemaId, "Schema not found").toString)
    }
  }

}
