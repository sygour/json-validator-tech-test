package controllers

import com.eclipsesource.schema.SchemaValidator
import factories.ValidatorService
import javax.inject._
import model.{Error, Success}
import play.api.Logging
import play.api.libs.json._
import play.api.mvc._
import repositories.JsonSchemaRepository

import scala.io.Source

/**
 * Handles requests dealing with Json document validation
 */
@Singleton
class JsonValidationController @Inject()(val controllerComponents: ControllerComponents,
                                         val jsonSchemaRepository: JsonSchemaRepository,
                                         val validator: ValidatorService)
  extends BaseController with Logging {

  val ACTION_VALIDATE = "validateDocument"
  val ERROR_SCHEMA_NOT_FOUND = "Schema not found"
  val ERROR_CONTENT_NOT_JSON = "Content is not valid Json"

  /**
   * Validate a JSON document against a schema
   */
  def validateJson(schemaId: String) = Action { implicit request: Request[AnyContent] =>
    val body = request.body.asJson

    // body is JSON + clean JSON
    body.map(removeNull) match {
      case None => BadRequest(Error(ACTION_VALIDATE, schemaId, ERROR_CONTENT_NOT_JSON).toJson)
      case Some(jsonBody) =>
        // validate json against schema
        jsonSchemaRepository.read(schemaId) match {
          case None => Ok(Error(ACTION_VALIDATE, schemaId, ERROR_SCHEMA_NOT_FOUND).toJson)
          case Some(schema) =>
            validator.validate(Source.fromString(schema), jsonBody) match {
              case JsSuccess(_, _) => Ok(Success(ACTION_VALIDATE, schemaId).toJson)
              case JsError(errors) => Ok(Error(ACTION_VALIDATE, schemaId, formatErrorMessages(errors)).toJson)
            }
        }
    }
  }

  /**
   * Removes nodes with null value
   */
  def removeNull(jsValue: JsValue): JsValue = jsValue match {
    case JsObject(objects) =>
      JsObject(objects.flatMap {
        case (_, JsNull) => None
        case (key, value) => Some(key, removeNull(value))
      })
    case any => any
  }

  /**
   * Returns a human readable error message
   */
  private def formatErrorMessages(errors: collection.Seq[(JsPath, collection.Seq[JsonValidationError])]) = {
    val errorMessages = for (err <- errors) yield {
      err match {
        case (node, messages) => s"${node.toJsonString}: ${messages.map(_.message).reduce(_ + ", " + _)}"
      }
    }
    errorMessages.reduce(_ + ", " + _)
  }
}
