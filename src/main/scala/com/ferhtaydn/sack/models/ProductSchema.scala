package com.ferhtaydn.sack.models

import java.io.{ ByteArrayInputStream, ByteArrayOutputStream }

import com.ferhtaydn.sack.model.Product
import com.sksamuel.avro4s.{ AvroInputStream, AvroOutputStream, RecordFormat, SchemaFor }
import org.apache.avro.Schema
import org.apache.avro.generic.GenericRecord

object ProductSchema {

  private val schemaFile = getClass.getResourceAsStream("/avro/product.avsc")

  // Schema parsed from the schema file
  val productSchema: Schema = {
    val schemaParser = new Schema.Parser
    schemaParser.parse(schemaFile)
  }

  // generate the same result at compile time.
  // val productAvroSchema = AvroSchema[Product]

  // Schema provided implicitly for avro4s code
  implicit val productSchemaImplicit: SchemaFor[Product] =
    new SchemaFor[Product] {
      override def apply(): Schema = productSchema
    }

  val format = RecordFormat[Product]

  def productAsBytes(p: Product): Array[Byte] = {
    val baos = new ByteArrayOutputStream
    val output = AvroOutputStream.binary[Product](baos)
    output.write(p)
    output.close()
    baos.toByteArray
  }

  def productAsData(p: Product): Array[Byte] = {
    val baos = new ByteArrayOutputStream
    val output = AvroOutputStream.data[Product](baos)
    output.write(p)
    output.close()
    baos.toByteArray
  }

  def productFromBytes(bytes: Array[Byte]): Product = {
    val in = new ByteArrayInputStream(bytes)
    val input = AvroInputStream.binary[Product](in)
    input.iterator.toSeq.head
  }

  def productAsJson(p: Product): String = {
    val baos = new ByteArrayOutputStream
    val output = AvroOutputStream.json[Product](baos)
    output.write(p)
    output.close()
    baos.toString("UTF-8")
  }

  def productToRecord(p: Product): GenericRecord = {
    format.to(p)
  }

  def productFromRecord(productRecord: GenericRecord): Product = {
    format.from(productRecord)
  }

}
