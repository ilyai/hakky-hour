package com.typesafe.training.hakkyhour

import akka.actor.{ ActorRef, Props, ActorLogging, Actor }
import com.typesafe.training.hakkyhour.HakkyHour.CreateGuest
import scala.concurrent.duration._

/**
 * Created by iig on 11/3/15.
 */

object HakkyHour {
  case class CreateGuest(favoriteDrink: Drink)

  def props = Props(new HakkyHour())
}

class HakkyHour extends Actor with ActorLogging {
  log.debug("{} has opened!", "Hakky Hour")

  val waiter = createWaiter()
  val barkeeper = createBarkeeper()

  def receive: Receive = {
    case CreateGuest(favoriteDrink) => createGuest(waiter, favoriteDrink)
  }

  def createWaiter() =
    context.actorOf(Waiter.props, "waiter")

  def createGuest(waiter: ActorRef, favoriteDrink: Drink) =
    context.actorOf(Guest.props(waiter, favoriteDrink,
      Duration(
        context.system.settings.config.getDuration("hakky-hour.guest.finish-drink-duration", MILLISECONDS),
        MILLISECONDS)))

  def createBarkeeper() =
    context.actorOf(Barkeeper.props(
      Duration(
        context.system.settings.config.getDuration("hakky-hour.guest.finish-drink-duration", MILLISECONDS),
        MILLISECONDS)), "barkeeper")
}
