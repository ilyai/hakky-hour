package com.typesafe.training.hakkyhour

import akka.actor.{ ActorRef, ActorLogging, Props, Actor }
import com.typesafe.training.hakkyhour.Guest.DrinkFinished
import com.typesafe.training.hakkyhour.Waiter.{ ServeDrink, DrinkServed }
import scala.concurrent.duration._

import scala.concurrent.duration.FiniteDuration

/**
 * Created by iig on 11/6/15.
 */

object Guest {
  private case object DrinkFinished

  def props(waiter: ActorRef, favoriteDrink: Drink, finishDrinkDuration: FiniteDuration) =
    Props(new Guest(waiter, favoriteDrink, finishDrinkDuration))
}

class Guest(waiter: ActorRef, favoriteDrink: Drink, finishDrinkDuration: FiniteDuration) extends Actor with ActorLogging {
  import context.dispatcher

  var drinkCount = 0

  waiter ! Waiter.ServeDrink(favoriteDrink)

  override def receive = {
    case DrinkServed(drink) =>
      drinkCount += 1
      log.debug("Enjoying my {}. yummy {}!", drinkCount, drink)
      context.system.scheduler.scheduleOnce(finishDrinkDuration, self, DrinkFinished)
    case DrinkFinished =>
      waiter ! ServeDrink(favoriteDrink)
  }
}
