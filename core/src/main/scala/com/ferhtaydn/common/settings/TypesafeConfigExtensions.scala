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

package com.ferhtaydn.common.settings

import java.util.Properties

import com.typesafe.config.Config

import scala.collection.JavaConversions._
import scala.collection.immutable._
import scala.collection.mutable

/**
 * Taken from
 * https://github.com/cakesolutions/scala-kafka-client/blob/master/client/src/main/scala/cakesolutions/kafka/TypesafeConfigExtensions.scala
 * Extensions added to Typesafe config class.
 */
object TypesafeConfigExtensions {

  implicit class RichConfig(val config: Config) extends AnyVal {

    /**
     * Convert Typesafe config to Java `Properties`.
     */
    def toProperties: Properties = {
      val props = new Properties()
      config.entrySet().foreach(entry ⇒ props.put(entry.getKey, entry.getValue.unwrapped().toString))
      props
    }

    /**
     * Convert Typesafe config to a Scala map.
     */
    def toPropertyMap: Map[String, AnyRef] = {
      val map = mutable.Map[String, AnyRef]()
      config.entrySet().foreach(entry ⇒ map.put(entry.getKey, entry.getValue.unwrapped().toString))
      map.toMap
    }
  }
}
