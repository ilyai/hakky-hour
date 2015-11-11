package com.typesafe.training.hakkyhour

import akka.actor.{ Actor, Props }
import com.typesafe.training.hakkyhour.Barkeeper.{ DrinkPrepared, PrepareDrink }

import scala.concurrent.duration.FiniteDuration

/**
 * Created by iig on 11/11/15.
 */

object Barkeeper {
  case class PrepareDrink(drink: Drink, guest: Guest)
  case class DrinkPrepared(drink: Drink, guest: Guest)

  def props(prepareDrinkDuration: FiniteDuration) =
    Props(new Barkeeper(prepareDrinkDuration))
}

class Barkeeper(prepareDrinkDuration: FiniteDuration) extends Actor {
  def receive = {
    case PrepareDrink(drink, guest) =>
      busy(prepareDrinkDuration)
      sender ! DrinkPrepared(drink, guest)
  }
}
