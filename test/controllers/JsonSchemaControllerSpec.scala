package controllers

import akka.stream.Materializer
import model.Success
import org.mockito.Mockito
import org.mockito.Mockito._
import org.mockito.ArgumentMatchersSugar
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.Logging
import play.api.test.Helpers._
import play.api.test._
import repositories.JsonSchemaRepository

import scala.io.Source

class JsonSchemaControllerSpec extends PlaySpec with GuiceOneAppPerTest with Injecting
  with MockitoSugar with ArgumentMatchersSugar {

  implicit lazy val materializer: Materializer = app.materializer

  "JsonSchemaController POST" should {

    "return a successful upload result" in {
      val schemaRepository = mock[JsonSchemaRepository]
      val controller = new JsonSchemaController(stubControllerComponents(), schemaRepository)

      val schemaId = "my-schema-id"
      val data = Source.fromResource("data/config-schema.json")
        .getLines().reduce(_+_)

      val request = FakeRequest(POST, "/schema/" + schemaId)
        .withHeaders((CONTENT_TYPE, JSON))
        .withTextBody(data)

      val upload = controller.uploadSchema(schemaId).apply(request)

      status(upload) mustBe CREATED
      contentType(upload) mustBe Some(JSON)
      contentAsString(upload) mustEqual Success(controller.UPLOAD_ACTION, schemaId).toString
    }
  }

  "JsonSchemaController GET" should {

    "return a stored schema" in {
      val schemaRepository = mock[JsonSchemaRepository]
      val controller = new JsonSchemaController(stubControllerComponents(), schemaRepository)

      val schemaId = "my-schema-id"
      val request = FakeRequest(GET, "/schema/" + schemaId)
      val jsonString = """{ "key": "value" }"""
      when(schemaRepository.read(schemaId)).thenReturn(Some(jsonString))

      val download = controller.downloadSchema(schemaId).apply(request)

      status(download) mustBe OK
      contentAsString(download) mustEqual jsonString
    }

    "return an error if schema not found" in {
      val schemaRepository = mock[JsonSchemaRepository]
      val controller = new JsonSchemaController(stubControllerComponents(), schemaRepository)

      val schemaId = "my-schema-id"
      val request = FakeRequest(GET, "/schema/" + schemaId)
      when(schemaRepository.read(schemaId)).thenReturn(None)

      val download = controller.downloadSchema(schemaId).apply(request)

      status(download) mustBe BAD_REQUEST
    }
  }
}
