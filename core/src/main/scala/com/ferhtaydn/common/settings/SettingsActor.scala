package com.ferhtaydn.common.settings

import akka.actor.Actor

trait SettingsActor { self: Actor ⇒
  val settings = Settings(context.system)
}
