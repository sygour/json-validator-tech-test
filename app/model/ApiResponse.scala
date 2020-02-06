package model

import play.api.libs.json.{JsValue, Json}

class ApiResponse(action: String, id: String, status: String, message: Option[String]) {
  val json = Json.obj(
    "action" -> action,
    "id" -> id,
    "status" -> status
  )

  override def toString: String = {
    message
      .map(m => json ++ Json.obj("message" -> m))
      .getOrElse(json)
      .toString
  }

  def toJson: JsValue = {
    Json.parse(toString)
  }
}

case class Success(action: String, id: String) extends ApiResponse(action, id, "success", None)
case class Error(action: String, id: String, message: String) extends ApiResponse(action, id, "error", Some(message))
