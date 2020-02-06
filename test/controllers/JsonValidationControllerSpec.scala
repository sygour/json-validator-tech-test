package controllers

import akka.stream.Materializer
import factories.ValidatorService
import model.{Error, Success}
import org.mockito.ArgumentMatchersSugar
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.libs.json._
import play.api.test.Helpers._
import play.api.test._
import repositories.JsonSchemaRepository

import scala.io.Source

class JsonValidationControllerSpec extends PlaySpec with GuiceOneAppPerTest with Injecting
  with MockitoSugar with ArgumentMatchersSugar {

  implicit lazy val materializer: Materializer = app.materializer

  "JsonSchemaController POST" should {

    "return an error when schema is not found" in {
      val schemaRepository = mock[JsonSchemaRepository]
      val validator = mock[ValidatorService]
      val controller = new JsonValidationController(stubControllerComponents(), schemaRepository, validator)

      val schemaId = "my-schema-id"
      when(schemaRepository.read(schemaId)).thenReturn(None)
      val data = Source.fromResource("data/config-1.json")
        .getLines().reduce(_+_)

      val request = FakeRequest(POST, "/validate/" + schemaId)
        .withHeaders((CONTENT_TYPE, JSON))
        .withJsonBody(Json.parse(data))

      val upload = controller.validateJson(schemaId).apply(request)

      status(upload) mustBe OK
      contentType(upload) mustBe Some(JSON)
      contentAsString(upload) mustEqual Error(controller.ACTION_VALIDATE, schemaId, controller.ERROR_SCHEMA_NOT_FOUND).toString
    }

    "return success if validation is successful" in {
      val schemaRepository = mock[JsonSchemaRepository]
      val validator = mock[ValidatorService]
      val controller = new JsonValidationController(stubControllerComponents(), schemaRepository, validator)

      val schemaId = "my-schema-id"
      val schemaContent = """{ "a": "b" }"""
      val data = Source.fromResource("data/config-1.json")
        .getLines().reduce(_+_)
      when(schemaRepository.read(schemaId))
        .thenReturn(Some(schemaContent))
      val jsSuccess = mock[JsSuccess[JsValue]]
      when(validator.validate(any[Source], any[JsValue]))
        .thenReturn(jsSuccess)

      val request = FakeRequest(POST, "/validate/" + schemaId)
        .withHeaders((CONTENT_TYPE, JSON))
        .withJsonBody(Json.parse(data))

      val upload = controller.validateJson(schemaId).apply(request)

      status(upload) mustBe OK
      contentType(upload) mustBe Some(JSON)
      contentAsString(upload) mustEqual Success(controller.ACTION_VALIDATE, schemaId).toString
    }

    "return error if validation has failed" in {
      val schemaRepository = mock[JsonSchemaRepository]
      val validator = mock[ValidatorService]
      val controller = new JsonValidationController(stubControllerComponents(), schemaRepository, validator)

      val schemaId = "my-schema-id"
      val schemaContent = """{ "a": "b" }"""
      val data = Source.fromResource("data/config-1.json")
        .getLines().reduce(_+_)
      when(schemaRepository.read(schemaId))
        .thenReturn(Some(schemaContent))
      val jsError = JsError(Seq(
        (JsPath \ "name", Seq(JsonValidationError("missing"))),
        (JsPath \ "address" \ "country", Seq(JsonValidationError("missing")))
      ))
      val expectedMessage = "obj.name: missing, obj.address.country: missing"
      when(validator.validate(any[Source], any[JsValue]))
        .thenReturn(jsError)

      val request = FakeRequest(POST, "/validate/" + schemaId)
        .withHeaders((CONTENT_TYPE, JSON))
        .withJsonBody(Json.parse(data))

      val upload = controller.validateJson(schemaId).apply(request)

      status(upload) mustBe OK
      contentType(upload) mustBe Some(JSON)
      contentAsString(upload) mustEqual Error(controller.ACTION_VALIDATE, schemaId, expectedMessage).toString
    }
  }
}
