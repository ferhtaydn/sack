package com.ferhtaydn.sack.models

import org.scalatest.{ BeforeAndAfterAll, Matchers, WordSpecLike }

import java.io.{ ByteArrayInputStream, ByteArrayOutputStream }

import com.ferhtaydn.sack.model.Product
import com.sksamuel.avro4s._
import org.apache.avro.Schema
import org.apache.avro.generic.GenericRecord

/**
 * Created by ferhat.aydin on 11/24/16.
 */
class ProductSchemaTest
    extends WordSpecLike
    with BeforeAndAfterAll
    with Matchers {

  "ProducSchemaTest" when {

    "product schema parsed from file" should {

      val productSchema: Schema = new Schema.Parser().parse(getClass.getResourceAsStream("/avro/product.avsc"))

      "full name should be correct" in {
        productSchema.getFullName shouldBe "com.ferhtaydn.sack.model.Product"
      }

      "schema type should be record" in {
        productSchema.getType shouldBe Schema.Type.RECORD
      }
    }

    "Product converted to GenericRecord" should {

      val productSchema = AvroSchema[Product]
      val p = ProductExt.dummyProduct
      val gr = ProductSchema.productToRecord(p)

      "return correct schema" in {
        gr.getSchema shouldBe productSchema
      }

      "return correct brand field" in {
        gr.get("brand") shouldBe "Brand"

      }
    }

    "GenericRecord converted to Product" should {

      val p = ProductExt.dummyProduct
      val gr = ProductSchema.productToRecord(p)

      "return correct schema" in {
        val product = ProductSchema.productFromRecord(gr)
        product shouldBe p
      }

    }
  }

}
