package com.typesafe.training.hakkyhour

import akka.actor.{ ActorRef, ActorLogging, Props, Actor }
import com.typesafe.training.hakkyhour.Guest.{ DrunkException, DrinkFinished }
import com.typesafe.training.hakkyhour.HakkyHour.NoMoreDrinks
import com.typesafe.training.hakkyhour.Waiter.{ ServeDrink, DrinkServed }
import scala.concurrent.duration._

import scala.concurrent.duration.FiniteDuration

/**
 * Created by iig on 11/6/15.
 */

object Guest {
  private case object DrinkFinished
  case object DrunkException extends IllegalStateException("Too many drinks!")

  def props(waiter: ActorRef, favoriteDrink: Drink, finishDrinkDuration: FiniteDuration,
    isStubborn: Boolean, maxDrinkCount: Int) =
    Props(new Guest(waiter, favoriteDrink, finishDrinkDuration, isStubborn: Boolean, maxDrinkCount: Int))
}

class Guest(waiter: ActorRef, favoriteDrink: Drink, finishDrinkDuration: FiniteDuration,
    isStubborn: Boolean, maxDrinkCount: Int) extends Actor with ActorLogging {
  import context.dispatcher

  var drinkCount = 0

  waiter ! Waiter.ServeDrink(favoriteDrink)

  override def postStop() =
    log.info("Good-bye!")

  override def receive = {
    case DrinkServed(drink) =>
      drinkCount += 1
      log.debug("Enjoying my {}. yummy {}!", drinkCount, drink)
      context.system.scheduler.scheduleOnce(finishDrinkDuration, self, DrinkFinished)
    case DrinkFinished =>
      if (drinkCount > maxDrinkCount)
        throw DrunkException
      waiter ! ServeDrink(favoriteDrink)
    case NoMoreDrinks =>
      if (isStubborn) {
        waiter ! ServeDrink(favoriteDrink)
      } else {
        log.info("All right, time to go home!")
        context.stop(self)
      }
  }
}
