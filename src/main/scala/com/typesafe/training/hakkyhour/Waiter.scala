package com.typesafe.training.hakkyhour

import akka.actor.{ Props, Actor }
import com.typesafe.training.hakkyhour.Waiter.{ DrinkServed, ServeDrink }

/**
 * Created by iig on 11/6/15.
 */

object Waiter {
  case class ServeDrink(drink: Drink)
  case class DrinkServed(drink: Drink)

  def props = Props(new Waiter)
}

class Waiter extends Actor {
  override def receive = {
    case ServeDrink(drink) => sender ! DrinkServed(drink)
  }
}
