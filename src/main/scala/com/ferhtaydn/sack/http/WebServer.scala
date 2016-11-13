package com.ferhtaydn.sack.http

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import com.ferhtaydn.sack.settings.Settings
import com.ferhtaydn.sack.model.Product

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
