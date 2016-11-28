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

package com.ferhtaydn.http

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import com.ferhtaydn.common.settings.Settings
import com.ferhtaydn.common.models.Product

import de.heikoseeberger.akkahttpcirce.CirceSupport._

import scala.io.StdIn

case class Products(products: Seq[Product])

object WebServer extends App {

  implicit val system = ActorSystem("product-api-system")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val httpSettings = Settings(system).Http

  val productApi = system.actorOf(ProductApiActor.props(), "product-api-actor")

  import io.circe.generic.auto._

  val route =
    path("product") {
      post {
        decodeRequest {
          entity(as[Product]) { product ⇒
            productApi ! Products(List(product))
            complete((StatusCodes.Accepted, "Product is saved to Kafka"))
          }
        }
      }
    } ~
      path("products") {
        post {
          decodeRequest {
            entity(as[Products]) { products ⇒
              system.log.info("products are sending to the product-api-actor")
              productApi ! products
              complete((StatusCodes.Accepted, "Products are saved to Kafka"))
            }
          }
        }
      }

  val bindingFuture = Http().bindAndHandle(route, httpSettings.host, httpSettings.port)

  println(s"Server online at ${httpSettings.host}:${httpSettings.port} \n Press RETURN to stop...")

  StdIn.readLine()

  bindingFuture.flatMap(_.unbind()).onComplete(_ ⇒ system.terminate())

}
