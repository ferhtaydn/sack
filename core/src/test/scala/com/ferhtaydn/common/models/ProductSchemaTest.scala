/*
 * Copyright 2016 Ferhat Aydın
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ferhtaydn.common.models

import org.scalatest.{ BeforeAndAfterAll, Matchers, WordSpecLike }

import com.sksamuel.avro4s._
import org.apache.avro.Schema

class ProductSchemaTest
    extends WordSpecLike
    with BeforeAndAfterAll
    with Matchers {

  "ProducSchemaTest" when {

    "product schema parsed from file" should {

      val productSchema: Schema = new Schema.Parser().parse(getClass.getResourceAsStream("/avro/product.avsc"))

      "full name should be correct" in {
        productSchema.getFullName shouldBe "com.ferhtaydn.common.models.Product"
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
