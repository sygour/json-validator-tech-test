package controllers

import javax.inject._
import play.api._
import play.api.mvc._

/**
 * Handles requests dealing with Json document validation
 */
@Singleton
class JsonValidationController @Inject()(val controllerComponents: ControllerComponents) extends BaseController {

  /**
   * Validate a JSON document against a schema
   */
  def validateJson(schemaId: String) = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index())
  }
}
