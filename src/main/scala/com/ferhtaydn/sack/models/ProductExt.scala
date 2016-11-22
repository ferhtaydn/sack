package com.ferhtaydn.sack.models

import com.ferhtaydn.sack.csv.CsvParser
import com.ferhtaydn.sack.model.Product

//noinspection ScalaStyle
object ProductExt {

  def createProduct(s: String): Option[Product] = {
    CsvParser[Product].parse(s.split(",", -1).toSeq).toOption.filter(isValid)
  }

  def isValid(p: Product): Boolean = {
    p.brand.nonEmpty && p.brand.length <= 30 &&
      p.supplierId.nonEmpty && p.supplierId.length <= 50 && (p.supplierId.toLong > 0) &&
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
    brand = "Mees",
    supplierId = "59",
    productType = "2",
    gender = Some("1"),
    ageGroup = Some("11"),
    category = Some("3021"),
    productFeature = "A13",
    productCode = "TRED_IUH2",
    webProductDesc = "Web Ürün Tanımı",
    productDesc = "Ürün Tanımı",
    supplierColor = "TASSS",
    colorFeature = Some("C01"),
    barcode = java.util.UUID.randomUUID.toString,
    supplierSize = "34",
    dsmSize = "B20",
    stockUnit = Some("Adet"),
    ftStockQuantity = Some(1),
    ftPurchasePriceVatInc = Some(10),
    psfVatInc = Some(20),
    tsfVatInc = Some(15),
    vatRate = Some(18),
    material = Some("Materyal"),
    composition = Some("Kompozisyon"),
    productionPlace = Some("IT"),
    productWeightKg = Some(1),
    productionContentWriting = Some("Ürün İçerik Yazısı"),
    productDetail = Some("Ürün Detayı"),
    sampleSize = Some("Numune Bedeni"),
    modelSize = Some("Manken Ölçüsü"),
    supplierProductCode = Some("Tedarikçi Ürün Kodu"),
    project = Some("Proje"),
    theme = Some("Tema"),
    trendLevel = Some("TF02"),
    designer = Some("Tasarımcı"),
    imageUrl = None
  )

}
