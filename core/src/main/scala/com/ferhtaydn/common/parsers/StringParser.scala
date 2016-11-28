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
      case "" ⇒ Success(None)
      case str ⇒ StringParser[A].parse(str).map(x ⇒ Some(x))
    }
  }

  implicit val booleanParser = new StringParser[Boolean] {
    override def parse(s: String): Try[Boolean] = s match {
      case "1" | "true" | "True" | "TRUE" ⇒ Success(true)
      case "0" | "false" | "False" | "FALSE" ⇒ Success(false)
      case _ ⇒ Failure(new RuntimeException("'" ++ s ++ "' is not a valid boolean"))
    }
  }

}
