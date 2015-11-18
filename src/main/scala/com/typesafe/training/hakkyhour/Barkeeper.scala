package com.typesafe.training.hakkyhour

import akka.actor._
import com.typesafe.training.hakkyhour.Barkeeper.{ DrinkPrepared, PrepareDrink }

import scala.concurrent.duration.FiniteDuration

/**
 * Created by iig on 11/11/15.
 */

object Barkeeper {
  case class PrepareDrink(drink: Drink, guest: ActorRef)
  case class DrinkPrepared(drink: Drink, guest: ActorRef)

  sealed trait State
  object State {
    case object Ready extends State
    case object Busy extends State
  }

  case class Data(waiter: Option[ActorRef])

  def props(prepareDrinkDuration: FiniteDuration, accuracy: Int) =
    Props(new Barkeeper(prepareDrinkDuration, accuracy))
}

class Barkeeper(prepareDrinkDuration: FiniteDuration, accuracy: Int) extends Actor with Stash
    with FSM[Barkeeper.State, Barkeeper.Data] {
  import context.dispatcher
  import Barkeeper._

  startWith(State.Ready, Data(None))

  when(State.Ready) {
    case Event(PrepareDrink(drink, guest), _) =>
      setTimer("drink-prepared", DrinkPrepared(
        if (util.Random.nextInt(100) < accuracy) drink else Drink.anyOther(drink), guest), prepareDrinkDuration)
      goto(State.Busy) using Data(Some(sender))
  }

  when(State.Busy) {
    case Event(drinkPrepared: DrinkPrepared, Data(Some(waiter))) =>
      waiter ! drinkPrepared
      unstashAll()
      goto(State.Ready) using Data(None)
    case _ =>
      stash()
      stay()
  }

  initialize()
}
