package com.typesafe.training.hakkyhour

import akka.actor.{ ExtensionKey, Actor, Extension, ExtendedActorSystem }

import scala.concurrent.duration._

/**
 * Created by iig on 11/17/15.
 */

object Settings extends ExtensionKey[Settings]

class Settings(system: ExtendedActorSystem) extends Extension {
  val maxComplaintCount =
    system.settings.config.getInt("hakky-hour.waiter.max-complaint-count")

  val finishDrinkDuration =
    Duration(
      system.settings.config.getDuration("hakky-hour.guest.finish-drink-duration", MILLISECONDS),
      MILLISECONDS)

  val prepareDrinkDuration =
    Duration(
      system.settings.config.getDuration("hakky-hour.guest.finish-drink-duration", MILLISECONDS),
      MILLISECONDS)

  val maxDrinkCount =
    system.settings.config.getInt("hakky-hour.max-drink-count")

  val statusTimeout =
    Duration(
      system.settings.config.getDuration("hakky-hour.status-timeout", MILLISECONDS), MILLISECONDS)

  val barkkeperAccuracy =
    system.settings.config.getInt("hakky-hour.barkeeper.accuracy")
}

trait SettingsActor {
  this: Actor =>

  val settings: Settings =
    Settings(context.system)
}
