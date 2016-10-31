package com.ferhtaydn.sack.http

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.stream.ActorMaterializer
import com.ferhtaydn.sack.model.Product
import spray.json.DefaultJsonProtocol._

import scala.io.StdIn

object WebServer extends App {

  case class Products(products: List[Product])

  implicit val productFormat = jsonFormat7(Product)
  implicit val productsFormat = jsonFormat1(Products)

  implicit val system = ActorSystem("product-producer-system")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val productProducer = system.actorOf(ProductProducerActor.props, "product-producer-actor")

  val route =
    path("product") {
      post {
        entity(as[Product]) { product ⇒
          productProducer ! product
          complete((StatusCodes.Accepted, "Product saved to Kafka"))
        }
      }
    }

  val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")

  StdIn.readLine()

  bindingFuture.flatMap(_.unbind()).onComplete(_ ⇒ system.terminate())

}
