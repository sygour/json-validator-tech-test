package controllers

import com.eclipsesource.schema.SchemaValidator
import factories.ValidatorFactory
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
                                         val validatorFactory: ValidatorFactory)
  extends BaseController with Logging {

  /**
   * Validate a JSON document against a schema
   */
  def validateJson(schemaId: String) = Action(parse.tolerantJson) { implicit request: Request[JsValue] =>
    val validator: SchemaValidator = validatorFactory.validator
    val body = request.body

    // body is JSON + clean JSON
    val jsonBody = removeNull(body)

    // validate json against schema
    jsonSchemaRepository.read(schemaId) match {
      case None => Ok(Error("validateDocument", schemaId, "Schema not found").toString)
      case Some(schema) =>
        validator.validate(Source.fromString(schema), jsonBody) match {
          case JsSuccess(_, _) => Ok(Success("validateDocument", schemaId).toString)
          case JsError(errors) => Ok(Error("validateDocument", schemaId, formatErrorMessages(errors)).toString)
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
    val formattedErrorMessage = errorMessages.map("[" + _ + "]").reduce(_ + ", " + _)
    formattedErrorMessage
  }
}
