package com.ferhtaydn.sack.csv

import shapeless._

import scala.util.{ Failure, Success, Try }

sealed trait CsvParser[A] {
  def parse(tokens: Seq[String]): Try[A]
}

object CsvParser {

  def apply[A](implicit parser: CsvParser[A]): CsvParser[A] = parser

  private def baseParser[A](f: Seq[String] ⇒ Try[A]): CsvParser[A] = new CsvParser[A] {
    override def parse(s: Seq[String]): Try[A] = f(s)
  }

  implicit val hnilParser: CsvParser[HNil] = baseParser {
    case Nil ⇒ Success(HNil)
    case _   ⇒ Failure(new RuntimeException("Extra Token"))
  }

  implicit def hconsParser[H: StringParser, T <: HList: CsvParser]: CsvParser[H :: T] = baseParser {
    case h +: t ⇒ for {
      hv ← StringParser[H].parse(h)
      tv ← CsvParser[T].parse(t)
    } yield hv :: tv
    case Nil ⇒ Failure(new RuntimeException("Less token"))
  }

  implicit def classParser[A, L <: HList](implicit
    gen: Generic.Aux[A, L],
    parser: CsvParser[L]): CsvParser[A] = new CsvParser[A] {
    override def parse(tokens: Seq[String]): Try[A] = parser.parse(tokens).map(gen.from)
  }

}
