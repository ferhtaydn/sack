package com.ferhtaydn.sack.http

import com.ferhtaydn.sack.model.{ Product, TProduct }

object Models {

  case object OK
  case class Products(products: Seq[Product])

  case class TProducts(products: Seq[TProduct])

}
