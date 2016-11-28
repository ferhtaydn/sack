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

package com.ferhtaydn.common

import akka.actor.ActorSystem

import scala.concurrent.Await
import scala.concurrent.duration._

trait Boot extends App {

  def terminate(system: ActorSystem): Unit = {
    scala.sys.addShutdownHook {
      println("Terminating...")
      system.terminate()
      Await.result(system.whenTerminated, 30.seconds)
      println("Terminated... Bye")
    }
  }

}
