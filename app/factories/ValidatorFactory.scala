package factories

import com.eclipsesource.schema.SchemaValidator
import com.eclipsesource.schema.drafts.Version7
import com.google.inject.ImplementedBy

@ImplementedBy(classOf[ValidatorFactoryImpl])
trait ValidatorFactory {
  def validator: SchemaValidator
}

class ValidatorFactoryImpl extends ValidatorFactory {
  private val innerValidator: SchemaValidator = SchemaValidator(Some(Version7))

  override def validator: SchemaValidator = innerValidator
}

