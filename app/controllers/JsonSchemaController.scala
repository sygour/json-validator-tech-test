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

  /**
   * Store a new Json Schema
   */
  def uploadSchema(schemaId: String) = Action(parse.json) { implicit request: Request[JsValue] =>
    jsonSchemaRepository.save(schemaId, request.body.toString)
    Created(
      Success("uploadSchema", schemaId).toString
    )
  }

  /**
   * Retrieve a Json Schema if it exists
   */
  def downloadSchema(schemaId: String) = Action { implicit request: Request[AnyContent] =>
    jsonSchemaRepository.read(schemaId) match {
      case Some(schema) => Ok(schema)
      case None => BadRequest(Error("downloadSchema", schemaId, "Schema not found").toString)
    }
  }

}
