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

class ProductExtTest
    extends WordSpecLike
    with BeforeAndAfterAll
    with Matchers {

  "ProductExtTest" when {

    val line =
      """
        |Brand,59,2,1,11,3021,A12,P1,WebDesc,ProductDesc,Black,C01,barcode1,33,B19,Count,1,10,20.00,15.00,18,Material,Composition,IT,1,Content,ProductDetail,sampleSize,modelSize,supplierProductCode,Project,Theme,TF01,Designer,
      """.stripMargin.trim

    val incorrectLine = "a,b,c"

    val randomField = java.util.UUID.randomUUID.toString

    val dummy = ProductExt.dummyProduct

    "product" should {

      "return Some(product) from correct csv line" in {
        val p = ProductExt.createProduct(line).get
        p.imageUrl shouldBe None
      }

      "return None from incorrect csv line" in {
        ProductExt.createProduct(incorrectLine) shouldBe None
      }

      "return valid for dummy product" in {
        ProductExt.isValid(dummy) shouldBe true
      }

      "return invalid for empty brand name" in {
        ProductExt.isValid(dummy.copy(brand = "")) shouldBe false
      }

      "return invalid for long brand name" in {
        ProductExt.isValid(dummy.copy(brand = randomField)) shouldBe false
      }

      "return invalid for empty supplier id" in {
        ProductExt.isValid(dummy.copy(supplierId = "")) shouldBe false
      }

      "return invalid for long supplier id" in {
        ProductExt.isValid(dummy.copy(supplierId = randomField + randomField)) shouldBe false
      }

      "return invalid for not numeric supplier id" in {
        ProductExt.isValid(dummy.copy(supplierId = randomField)) shouldBe false
      }

      "return valid for null gender" in {
        ProductExt.isValid(dummy.copy(gender = None)) shouldBe true
      }

      "return invalid for long gender" in {
        ProductExt.isValid(dummy.copy(gender = Some(randomField))) shouldBe false
      }

    }
  }

}
