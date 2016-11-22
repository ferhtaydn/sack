package com.ferhtaydn.sack.settings

import org.scalatest.{ BeforeAndAfterAll, Matchers, WordSpecLike }
import com.typesafe.config.ConfigFactory
import com.ferhtaydn.sack.settings.TypesafeConfigExtensions._

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
