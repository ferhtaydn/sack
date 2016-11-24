package com.ferhtaydn.common.models

import com.ferhtaydn.common.parsers.{ CsvParser, StringParser }

//noinspection ScalaStyle
object ProductExt {

  def createProduct(s: String): Option[Product] = {
    CsvParser[Product].parse(s.split(",", -1).toSeq).toOption
  }

  def isValid(p: Product): Boolean = {
    p.brand.nonEmpty && p.brand.length <= 30 &&
      p.supplierId.nonEmpty && p.supplierId.length <= 50 &&
      StringParser.longParser.parse(p.supplierId).toOption.exists(_ > 0) &&
      p.productType.nonEmpty && p.productType.length <= 50 &&
      p.gender.forall(_.length <= 10) &&
      p.ageGroup.forall(_.length <= 3000) &&
      p.category.forall(_.length <= 300) &&
      p.productFeature.nonEmpty && p.productFeature.length <= 30 &&
      p.productCode.nonEmpty && p.productCode.length <= 30 &&
      p.webProductDesc.nonEmpty && p.webProductDesc.length <= 100 &&
      p.productDesc.nonEmpty && p.productDesc.length <= 100 &&
      p.supplierColor.nonEmpty && p.supplierColor.length <= 50 &&
      p.colorFeature.forall(_.length <= 5) &&
      p.barcode.nonEmpty && p.barcode.length <= 20 &&
      p.supplierSize.nonEmpty && p.supplierSize.length <= 50 &&
      p.dsmSize.nonEmpty && p.dsmSize.length <= 20 &&
      p.stockUnit.forall(_.length <= 10) &&
      p.material.forall(_.length <= 30) &&
      p.composition.forall(_.length <= 50) &&
      p.productionPlace.forall(_.length <= 10) &&
      p.productionContentWriting.forall(_.length <= 4000) &&
      p.productDetail.forall(_.length <= 100) &&
      p.sampleSize.forall(_.length <= 100) &&
      p.modelSize.forall(_.length <= 100) &&
      p.supplierProductCode.forall(_.length <= 30) &&
      p.project.forall(_.length <= 50) &&
      p.theme.forall(_.length <= 20) &&
      p.trendLevel.forall(_.length <= 10) &&
      p.designer.forall(_.length <= 20)
  }

  def dummyProduct = Product(
    "Brand", "59", "2", Some("1"), Some("11"), Some("3021"),
    "A12", "P1", "WebDesc", "ProductDesc", "Black", Some("C01"),
    java.util.UUID.randomUUID.toString.take(20),
    "33", "B19", Some("Count"), Some(1), Some(10.0), Some(20.0), Some(15.0), Some(18.0),
    Some("Material"), Some("Composition"), Some("IT"), Some(1.0),
    Some("Content"), Some("ProductDetail"), Some("sampleSize"), Some("modelSize"), Some("supplierProductCode"),
    Some("Project"), Some("Theme"), Some("TF01"), Some("Designer"), None
  )

}
