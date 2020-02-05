package controllers

import javax.inject._
import play.api._
import play.api.mvc._

/**
 * Handles request dealing with Json schemas
 */
@Singleton
class JsonSchemaController @Inject()(val controllerComponents: ControllerComponents) extends BaseController {

  /**
   * Store a new Json Schema
   */
  def uploadSchema(schemaId: String) = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index())
  }

  /**
   * Retrieve a Json Schema if it exists
   */
  def downloadSchema(schemaId: String) = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index())
  }

}
