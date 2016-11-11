package com.ferhtaydn.sack

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
