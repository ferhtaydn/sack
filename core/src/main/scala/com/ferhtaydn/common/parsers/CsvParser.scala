/*
 * Copyright 2016 Ferhat Aydın
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ferhtaydn.common.parsers

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
    case _ ⇒ Failure(new RuntimeException("Extra Token"))
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
