package com.ferhtaydn.sack.http

import com.ferhtaydn.sack.model.Product
import spray.json.DefaultJsonProtocol._

object Models {

  case object OK
  case class Products(products: Seq[Product])

  implicit val productFormat = jsonFormat7(Product)
  implicit val productsFormat = jsonFormat1(Products)

}
