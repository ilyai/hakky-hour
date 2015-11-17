package com.typesafe.training.hakkyhour

import akka.actor.{ Stash, ActorRef, Actor, Props }
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

class Barkeeper(prepareDrinkDuration: FiniteDuration, accuracy: Int) extends Actor with Stash {
  import context.dispatcher

  override def receive = ready

  def ready: Receive = {
    case PrepareDrink(drink, guest) =>
      context.system.scheduler.scheduleOnce(prepareDrinkDuration, self, DrinkPrepared(
        if (util.Random.nextInt(100) < accuracy) drink else Drink.anyOther(drink), guest))
      context.become(busy(sender))
  }

  def busy(waiter: ActorRef): Receive = {
    case drinkPrepared: DrinkPrepared =>
      waiter ! drinkPrepared
      unstashAll()
      context.become(ready)
    case _ =>
      stash()
  }
}
