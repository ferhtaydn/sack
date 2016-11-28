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

package com.ferhtaydn.common.settings

import org.scalatest.{ BeforeAndAfterAll, Matchers, WordSpecLike }
import com.typesafe.config.ConfigFactory
import com.ferhtaydn.common.settings.TypesafeConfigExtensions._

class TypesafeConfigExtensionsTest
    extends WordSpecLike
    with BeforeAndAfterAll
    with Matchers {

  "TypesafeConfigExtension" when {

    val config = ConfigFactory.parseString(
      """
        |http {
        |  host = "localhost"
        |  port = 8080
        |}
      """.stripMargin
    )

    "config is parsed from string" should {

      "return correct values for each config item" in {
        config.getInt("http.port") shouldBe 8080
        config.getString("http.host") shouldBe "localhost"
      }

      "be converted to Java properties correctly" in {
        config.toProperties.getProperty("http.port").toInt shouldBe 8080
        config.toProperties.getProperty("http.host") shouldBe "localhost"
      }

      "be converted to properties map correctly" in {
        config.toPropertyMap shouldBe Map("http.host" → "localhost", "http.port" → "8080")
      }
    }
  }
}
