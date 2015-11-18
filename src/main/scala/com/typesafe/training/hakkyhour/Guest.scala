package com.typesafe.training.hakkyhour

import akka.actor._
import com.typesafe.training.hakkyhour.Guest.{ DrunkException, DrinkFinished }
import com.typesafe.training.hakkyhour.HakkyHour.NoMoreDrinks
import com.typesafe.training.hakkyhour.Waiter.{ Complaint, ServeDrink, DrinkServed }
import scala.concurrent.duration._

import scala.concurrent.duration.FiniteDuration

/**
 * Created by iig on 11/6/15.
 */

object Guest {
  private case object DrinkFinished
  case object DrunkException extends IllegalStateException("Too many drinks!")

  trait State

  object State {
    case object Drinking extends State
    case object Waiting extends State
  }

  case class Data(drinkCount: Int)

  def props(waiter: ActorRef, favoriteDrink: Drink, finishDrinkDuration: FiniteDuration,
    isStubborn: Boolean, maxDrinkCount: Int) =
    Props(new Guest(waiter, favoriteDrink, finishDrinkDuration, isStubborn: Boolean, maxDrinkCount: Int))
}

class Guest(waiter: ActorRef, favoriteDrink: Drink, finishDrinkDuration: FiniteDuration,
    isStubborn: Boolean, maxDrinkCount: Int) extends Actor with ActorLogging with FSM[Guest.State, Guest.Data] {
  import context.dispatcher
  import Guest._

  var drinkCount = 0

  startWith(State.Waiting, Data(0))

  waiter ! Waiter.ServeDrink(favoriteDrink)

  override def postStop() =
    log.info("Good-bye!")

  when(State.Waiting) {
    case Event(Waiter.DrinkServed(`favoriteDrink`), Data(drinkCount)) =>
      goto(State.Drinking) using Data(drinkCount + 1)
    case Event(Waiter.DrinkServed(drink), _) =>
      log.info("Expected a {}, but got a {}!", favoriteDrink, drink)
      waiter ! Waiter.Complaint(favoriteDrink)
      stay()
    case Event(HakkyHour.NoMoreDrinks, _) if isStubborn =>
      waiter ! Waiter.ServeDrink(favoriteDrink)
      stay()
    case Event(HakkyHour.NoMoreDrinks, _) =>
      log.info("All right, time to go home!")
      stop()
  }

  when(State.Drinking, finishDrinkDuration) {
    case Event(StateTimeout, Data(drinkCount)) if drinkCount > maxDrinkCount =>
      throw DrunkException
    case Event(StateTimeout, _) =>
      waiter ! Waiter.ServeDrink(favoriteDrink)
      goto(State.Waiting)
  }

  onTransition {
    case State.Waiting -> State.Drinking =>
      log.info("Enjoying my {}. yummy {}!", nextStateData.drinkCount, favoriteDrink)
  }

  initialize()

}
