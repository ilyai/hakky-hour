package com.typesafe.training.hakkyhour

import akka.actor._
import com.typesafe.training.hakkyhour.Barkeeper.PrepareDrink
import com.typesafe.training.hakkyhour.Guest.DrunkException
import com.typesafe.training.hakkyhour.HakkyHour.{ GetStatus, NoMoreDrinks, ApproveDrink, CreateGuest }
import com.typesafe.training.hakkyhour.Waiter.FrustratedException
import scala.concurrent.duration._

/**
 * Created by iig on 11/3/15.
 */

object HakkyHour {
  case class CreateGuest(favoriteDrink: Drink, isStubborn: Boolean, maxDrinkCount: Int)
  case class ApproveDrink(drink: Drink, guest: ActorRef)
  case class Status(guestCount: Int)
  case object NoMoreDrinks
  case object GetStatus

  def props(maxDrinkCount: Int) =
    Props(new HakkyHour(maxDrinkCount: Int))
}

class HakkyHour(maxDrinkCount: Int) extends Actor with ActorLogging with SettingsActor {
  log.debug("{} has opened!", "Hakky Hour")

  override val supervisorStrategy =
    OneForOneStrategy() {
      case DrunkException => SupervisorStrategy.Stop
      case FrustratedException(drink, guest) =>
        barkeeper.tell(PrepareDrink(drink, guest), sender())
        SupervisorStrategy.Restart
    }

  val waiter = createWaiter()
  val barkeeper = createBarkeeper()

  var guestDrinkCount = Map[ActorRef, Int]()

  def receive: Receive = {
    case CreateGuest(favoriteDrink, isStubborn, maxDrinkCount) =>
      context.watch(createGuest(waiter, favoriteDrink, isStubborn, maxDrinkCount))
    case ApproveDrink(drink, guest) =>
      val drinkCount = guestDrinkCount.getOrElse(guest, 0)
      if (drinkCount < maxDrinkCount) {
        guestDrinkCount += guest -> (drinkCount + 1)
        barkeeper.forward(PrepareDrink(drink, guest))
      } else if (drinkCount == maxDrinkCount) {
        log.info("Sorry, {}, but we won't serve you more than {} drinks!", guest.path.name, maxDrinkCount)
        guestDrinkCount += guest -> (drinkCount + 1)
        guest ! NoMoreDrinks
      } else {
        guest ! PoisonPill
      }
    case Terminated(guest) =>
      guestDrinkCount -= guest
      log.info("Thanks, {}, for being our guest!", guest.path.name)
    case GetStatus =>
      sender ! HakkyHour.Status(guestDrinkCount.size)
  }

  def createWaiter() =
    context.actorOf(Waiter.props(self, settings.maxComplaintCount), "waiter")

  def createGuest(waiter: ActorRef, favoriteDrink: Drink, isStubborn: Boolean, maxDrinkCount: Int) =
    context.actorOf(Guest.props(waiter, favoriteDrink,
      settings.finishDrinkDuration, isStubborn, maxDrinkCount))

  def createBarkeeper() =
    context.actorOf(Barkeeper.props(
      settings.prepareDrinkDuration,
      settings.barkkeperAccuracy), "barkeeper")
}
