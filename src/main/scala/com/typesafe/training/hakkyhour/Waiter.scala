package com.typesafe.training.hakkyhour

import akka.actor.{ ActorRef, Props, Actor }
import com.typesafe.training.hakkyhour.Barkeeper.DrinkPrepared
import com.typesafe.training.hakkyhour.HakkyHour.ApproveDrink
import com.typesafe.training.hakkyhour.Waiter.{ DrinkServed, ServeDrink }

/**
 * Created by iig on 11/6/15.
 */

object Waiter {
  case class ServeDrink(drink: Drink)
  case class DrinkServed(drink: Drink)

  def props(hakkyHour: ActorRef) =
    Props(new Waiter(hakkyHour))
}

class Waiter(hakkyHour: ActorRef) extends Actor {
  override def receive = {
    case ServeDrink(drink) =>
      hakkyHour ! ApproveDrink(drink, sender())
    case DrinkPrepared(drink, guest) =>
      guest ! DrinkServed(drink)
  }
}
