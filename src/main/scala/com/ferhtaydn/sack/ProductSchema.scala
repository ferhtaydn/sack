package com.ferhtaydn.sack

import java.io.{ ByteArrayInputStream, ByteArrayOutputStream }

import com.ferhtaydn.sack.csv.CsvParser
import com.sksamuel.avro4s.{ AvroInputStream, AvroOutputStream, RecordFormat, SchemaFor }
import org.apache.avro.Schema
import com.ferhtaydn.sack.model.Product
import org.apache.avro.generic.GenericRecord

import scala.util.{ Success, Try }

//noinspection ScalaStyle
object ProductSchema {
  // Schema file as a input stream
  private val schemaFile = getClass.getResourceAsStream("/avro/product.avsc")

  // Schema parsed from the schema file
  val productSchema: Schema = {
    val schemaParser = new Schema.Parser
    schemaParser.parse(schemaFile)
  }

  // generate the same result at compile time.
  // val productAvroSchema = AvroSchema[Product]

  // Schema provided implicitly for avro4s code
  implicit val productSchemaImplicit: SchemaFor[Product] =
    new SchemaFor[Product] {
      override def apply(): Schema = productSchema
    }

  def productAsBytes(p: Product): Array[Byte] = {
    val baos = new ByteArrayOutputStream
    val output = AvroOutputStream.binary[Product](baos)
    output.write(p)
    output.close()
    baos.toByteArray
  }

  def productAsData(p: Product): Array[Byte] = {
    val baos = new ByteArrayOutputStream
    val output = AvroOutputStream.data[Product](baos)
    output.write(p)
    output.close()
    baos.toByteArray
  }

  def productFromBytes(bytes: Array[Byte]): Product = {
    val in = new ByteArrayInputStream(bytes)
    val input = AvroInputStream.binary[Product](in)
    input.iterator.toSeq.head
  }

  def productAsJson(p: Product): String = {
    val baos = new ByteArrayOutputStream
    val output = AvroOutputStream.json[Product](baos)
    output.write(p)
    output.close()
    baos.toString("UTF-8")
  }

  def productToRecord(p: Product): GenericRecord = {
    val format = RecordFormat[Product]
    format.to(p)
  }

  def productFromRecord(productRecord: GenericRecord): Product = {
    val format = RecordFormat[Product]
    format.from(productRecord)
  }

  def createProduct(s: String): Option[Product] = {

    println(s"current value: $s")

    val arr = s.split(",", -1)

    println(s"current arr: ${arr.toList} \n ${arr.length}")

    CsvParser[Product].parse(arr.toSeq).toOption.filter { p ⇒

      p.brand.nonEmpty && p.brand.length <= 30 &&
        p.supplierId.nonEmpty && p.supplierId.length <= 50 && (p.supplierId.toLong > 0) &&
        p.productType.nonEmpty && p.productType.length <= 50 &&
        p.gender.exists(_.length <= 10) &&
        p.ageGroup.exists(_.length <= 3000) &&
        p.category.exists(_.length <= 300) &&
        p.productFeature.nonEmpty && p.productFeature.length <= 30 &&
        p.productCode.nonEmpty && p.productCode.length <= 30 &&
        p.webProductDesc.nonEmpty && p.webProductDesc.length <= 100 &&
        p.productDesc.nonEmpty && p.productDesc.length <= 100 &&
        p.supplierColor.nonEmpty && p.supplierColor.length <= 50 &&
        p.colorFeature.exists(_.length <= 5) &&
        p.barcode.nonEmpty && p.barcode.length <= 20 &&
        p.supplierSize.nonEmpty && p.supplierSize.length <= 50 &&
        p.dsmSize.nonEmpty && p.dsmSize.length <= 20 &&
        p.stockUnit.exists(_.length <= 10) &&
        p.material.exists(_.length <= 30) &&
        p.composition.exists(_.length <= 50) &&
        p.productionPlace.exists(_.length <= 10) &&
        p.productionContentWriting.exists(_.length <= 4000) &&
        p.productDetail.exists(_.length <= 100) &&
        p.sampleSize.exists(_.length <= 100) &&
        p.modelSize.exists(_.length <= 100) &&
        p.supplierProductCode.exists(_.length <= 30) &&
        p.project.exists(_.length <= 50) &&
        p.theme.exists(_.length <= 20) &&
        p.trendLevel.exists(_.length <= 10) &&
        p.designer.exists(_.length <= 20)

    }
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
