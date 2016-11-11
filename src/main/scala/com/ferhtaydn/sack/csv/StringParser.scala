package com.ferhtaydn.sack.csv

import scala.util.{ Failure, Success, Try }

sealed trait StringParser[A] {
  def parse(s: String): Try[A]
}

object StringParser {

  def apply[A](implicit parser: StringParser[A]): StringParser[A] = parser

  private def baseParser[A](f: String ⇒ Try[A]): StringParser[A] = new StringParser[A] {
    override def parse(s: String): Try[A] = f(s)
  }

  implicit val stringParser: StringParser[String] = baseParser(Success(_))
  implicit val intParser: StringParser[Int] = baseParser(s ⇒ Try(s.toInt))
  implicit val longParser: StringParser[Long] = baseParser(s ⇒ Try(s.toLong))
  implicit val doubleParser: StringParser[Double] = baseParser(s ⇒ Try(s.toDouble))
  implicit val floatParser: StringParser[Float] = baseParser(s ⇒ Try(s.toFloat))

  implicit def optionParser[A: StringParser]: StringParser[Option[A]] = new StringParser[Option[A]] {
    override def parse(s: String): Try[Option[A]] = s match {
      case ""  ⇒ Success(None)
      case str ⇒ StringParser[A].parse(str).map(x ⇒ Some(x))
    }
  }

  implicit val booleanParser = new StringParser[Boolean] {
    override def parse(s: String): Try[Boolean] = s match {
      case "1" | "true" | "True" | "TRUE"    ⇒ Success(true)
      case "0" | "false" | "False" | "FALSE" ⇒ Success(false)
      case _                                 ⇒ Failure(new RuntimeException("'" ++ s ++ "' is not a valid boolean"))
    }
  }

}