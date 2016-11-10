package com.ferhtaydn.sack

import java.io.{ ByteArrayInputStream, ByteArrayOutputStream }

import com.sksamuel.avro4s.{ AvroInputStream, AvroOutputStream, RecordFormat, SchemaFor }
import org.apache.avro.Schema
import com.ferhtaydn.sack.model.{ Product, TProduct }
import org.apache.avro.generic.GenericRecord

import scala.util.{ Random, Try }

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

  // Schema file as a input stream
  private val tSchemaFile = getClass.getResourceAsStream("/avro/tproduct.avsc")

  // Schema parsed from the schema file
  val tProductSchema: Schema = {
    val schemaParser = new Schema.Parser
    schemaParser.parse(tSchemaFile)
  }

  implicit val tProductSchemaImplicit: SchemaFor[TProduct] =
    new SchemaFor[TProduct] {
      override def apply(): Schema = tProductSchema
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

  def tProductToRecord(p: TProduct): GenericRecord = {
    val format = RecordFormat[TProduct]
    format.to(p)
  }

  def productFromRecord(productRecord: GenericRecord): Product = {
    val format = RecordFormat[Product]
    format.from(productRecord)
  }

  def createTProduct(s: String): Option[TProduct] = {

    println(s"current value: $s")

    val arr = s.split(",")

    println(s"current arr: ${arr.toList} \n ${arr.length}")

    if (arr.length > 33) {

      val brand = arr(0)
      val supplierId = arr(1)
      val productType = arr(2)
      val gender = arr(3)
      val ageGroup = arr(4)
      val category = arr(5)
      val productFeature = arr(6)
      val productCode = arr(7)
      val webProductDesc = arr(8)
      val productDesc = arr(9)
      val supplierColor = arr(10)
      val colorFeature = arr(11)
      val barcode = arr(12)
      val supplierSize = arr(13)
      val dsmSize = arr(14)
      val stockUnit = arr(15)
      val ftStockQuantity = arr(16)
      val ftPurchasePriceVatInc = arr(17)
      val psfVatInc = arr(18)
      val tsfVatInc = arr(19)
      val vatRate = arr(20)
      val material = arr(21)
      val composition = arr(22)
      val productionPlace = arr(23)
      val productWeightKg = arr(24)
      val productionContentWriting = arr(25)
      val productDetail = arr(26)
      val sampleSize = arr(27)
      val modelSize = arr(28)
      val supplierProductCode = arr(29)
      val project = arr(30)
      val theme = arr(31)
      val trendLevel = arr(32)
      val designer = arr(33)
      val imageUrl = if (arr.length == 35) arr(34) else ""

      if (brand.nonEmpty && brand.length <= 30 &&
        supplierId.nonEmpty && supplierId.length <= 50 && Try(supplierId.toLong).toOption.exists(id ⇒ id > 0) &&
        productType.nonEmpty && productType.length <= 50 &&
        gender.length <= 10 &&
        ageGroup.length <= 300 &&
        category.length <= 300 &&
        productFeature.nonEmpty && productFeature.length <= 30 &&
        productCode.nonEmpty && productCode.length <= 30 &&
        webProductDesc.nonEmpty && webProductDesc.length <= 100 &&
        productDesc.nonEmpty && productDesc.length <= 100 &&
        supplierColor.nonEmpty && supplierColor.length <= 50 &&
        colorFeature.length <= 5 &&
        barcode.nonEmpty && barcode.length <= 20 &&
        supplierSize.nonEmpty && supplierSize.length <= 50 &&
        dsmSize.nonEmpty && dsmSize.length <= 20 &&
        stockUnit.length <= 10 &&
        material.length <= 30 &&
        composition.length <= 50 &&
        productionPlace.length <= 10 &&
        productionContentWriting.length <= 4000 &&
        productDetail.length <= 100 &&
        sampleSize.length <= 100 &&
        modelSize.length <= 100 &&
        supplierProductCode.length <= 30 &&
        project.length <= 50 &&
        theme.length <= 20 &&
        trendLevel.length <= 10 &&
        designer.length <= 20) {

        val tp = TProduct(
          brand,
          supplierId,
          productType,
          if (gender.nonEmpty) Some(gender) else None,
          if (ageGroup.nonEmpty) Some(ageGroup) else None,
          if (category.nonEmpty) Some(category) else None,
          productFeature,
          productCode,
          webProductDesc,
          productDesc,
          supplierColor,
          if (colorFeature.nonEmpty) Some(colorFeature) else None,
          barcode,
          supplierSize,
          dsmSize,
          if (stockUnit.nonEmpty) Some(stockUnit) else None,
          Try(ftStockQuantity.toInt).toOption,
          Try(ftPurchasePriceVatInc.toDouble).toOption,
          Try(psfVatInc.toDouble).toOption,
          Try(tsfVatInc.toDouble).toOption,
          Try(vatRate.toDouble).toOption,
          if (material.nonEmpty) Some(material) else None,
          if (composition.nonEmpty) Some(composition) else None,
          if (productionPlace.nonEmpty) Some(productionPlace) else None,
          Try(productWeightKg.toDouble).toOption,
          if (productionContentWriting.nonEmpty) Some(productionContentWriting) else None,
          if (productDetail.nonEmpty) Some(productDetail) else None,
          if (sampleSize.nonEmpty) Some(sampleSize) else None,
          if (modelSize.nonEmpty) Some(modelSize) else None,
          if (supplierProductCode.nonEmpty) Some(supplierProductCode) else None,
          if (project.nonEmpty) Some(project) else None,
          if (theme.nonEmpty) Some(theme) else None,
          if (trendLevel.nonEmpty) Some(trendLevel) else None,
          if (designer.nonEmpty) Some(designer) else None,
          if (imageUrl.nonEmpty) Some(imageUrl) else None
        )

        Some(tp)

      } else None

    } else {
      None
    }
  }

  def dummyProduct = Product(java.util.UUID.randomUUID.toString, "brand" + Random.nextInt(10).toString,
    1, 2, 3, 4, "http" + Random.nextInt(10).toString)
}
