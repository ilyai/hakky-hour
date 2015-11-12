package com.typesafe.training.hakkyhour

import akka.actor._
import com.typesafe.training.hakkyhour.Barkeeper.PrepareDrink
import com.typesafe.training.hakkyhour.HakkyHour.{ NoMoreDrinks, ApproveDrink, CreateGuest }
import scala.concurrent.duration._

/**
 * Created by iig on 11/3/15.
 */

object HakkyHour {
  case class CreateGuest(favoriteDrink: Drink, isStubborn: Boolean)
  case class ApproveDrink(drink: Drink, guest: ActorRef)
  case object NoMoreDrinks

  def props(maxDrinkCount: Int) =
    Props(new HakkyHour(maxDrinkCount: Int))
}

class HakkyHour(maxDrinkCount: Int) extends Actor with ActorLogging {
  log.debug("{} has opened!", "Hakky Hour")

  val waiter = createWaiter()
  val barkeeper = createBarkeeper()

  var guestDrinkCount = Map[ActorRef, Int]()

  def receive: Receive = {
    case CreateGuest(favoriteDrink, isStubborn) =>
      context.watch(createGuest(waiter, favoriteDrink, isStubborn))
    case ApproveDrink(drink, guest) =>
      val drinkCount = guestDrinkCount.getOrElse(guest, 0)
      if (drinkCount < maxDrinkCount) {
        guestDrinkCount += guest -> (drinkCount + 1)
        barkeeper.forward(PrepareDrink(drink, guest))
      } else if (drinkCount == maxDrinkCount) {
        log.info("Sorry, {}, but we won't serve you more than {} drinks!", guest, maxDrinkCount)
        guestDrinkCount += guest -> (drinkCount + 1)
        guest ! NoMoreDrinks
      } else {
        guest ! PoisonPill
      }
    case Terminated(guest) =>
      guestDrinkCount -= guest
      log.info("Thanks, {}, for being our guest!", guest)
  }

  def createWaiter() =
    context.actorOf(Waiter.props(self), "waiter")

  def createGuest(waiter: ActorRef, favoriteDrink: Drink, isStubborn: Boolean) =
    context.actorOf(Guest.props(waiter, favoriteDrink,
      Duration(
        context.system.settings.config.getDuration("hakky-hour.guest.finish-drink-duration", MILLISECONDS),
        MILLISECONDS), isStubborn))

  def createBarkeeper() =
    context.actorOf(Barkeeper.props(
      Duration(
        context.system.settings.config.getDuration("hakky-hour.guest.finish-drink-duration", MILLISECONDS),
        MILLISECONDS)), "barkeeper")
}
