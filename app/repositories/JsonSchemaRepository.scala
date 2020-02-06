package repositories

import java.io.FileWriter
import java.nio.file.{Files, Paths}

import com.google.inject.ImplementedBy

@ImplementedBy(classOf[JsonSchemaRepositoryImpl])
trait JsonSchemaRepository {
  def save(schemaId: String, content: String): Unit

  def read(schemaId: String): Option[String]
}

class JsonSchemaRepositoryImpl extends JsonSchemaRepository {
  def save(schemaId: String, content: String) = {
    val writer = new FileWriter(s"data-store/${schemaId}")
    writer.write(content)
    writer.close()
  }

  def read(schemaId: String): Option[String] = {
    Files.lines(Paths.get(s"data-store/${schemaId}"))
      .reduce(_ + _)
      .map(Option(_))
      .orElse(None)
  }
}
