package factories

import com.eclipsesource.schema.SchemaValidator
import com.eclipsesource.schema.drafts.Version7
import com.google.inject.ImplementedBy
import play.api.libs.json.{JsResult, JsValue}

import scala.io.Source

@ImplementedBy(classOf[ValidatorServiceImpl])
trait ValidatorService {
  def validate(source: Source, json: JsValue): JsResult[JsValue]
}

class ValidatorServiceImpl extends ValidatorService {
  private val innerValidator: SchemaValidator = SchemaValidator(Some(Version7))

  override def validate(source: Source, json: JsValue) = innerValidator.validate(source, json)
}
