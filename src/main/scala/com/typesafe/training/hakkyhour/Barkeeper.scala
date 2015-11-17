package com.typesafe.training.hakkyhour

import akka.actor.{ ActorRef, Actor, Props }
import com.typesafe.training.hakkyhour.Barkeeper.{ DrinkPrepared, PrepareDrink }

import scala.concurrent.duration.FiniteDuration

/**
 * Created by iig on 11/11/15.
 */

object Barkeeper {
  case class PrepareDrink(drink: Drink, guest: ActorRef)
  case class DrinkPrepared(drink: Drink, guest: ActorRef)

  def props(prepareDrinkDuration: FiniteDuration, accuracy: Int) =
    Props(new Barkeeper(prepareDrinkDuration, accuracy))
}

class Barkeeper(prepareDrinkDuration: FiniteDuration, accuracy: Int) extends Actor {
  def receive = {
    case PrepareDrink(drink, guest) =>
      busy(prepareDrinkDuration)
      sender ! DrinkPrepared(
        if (util.Random.nextInt(100) < accuracy) drink else Drink.anyOther(drink), guest)
  }
}
