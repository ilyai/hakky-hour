package com.typesafe.training.hakkyhour

import akka.actor.{ ActorRef, Props, Actor }
import com.typesafe.training.hakkyhour.Barkeeper.DrinkPrepared
import com.typesafe.training.hakkyhour.HakkyHour.ApproveDrink
import com.typesafe.training.hakkyhour.Waiter.{ FrustratedException, Complaint, DrinkServed, ServeDrink }

/**
 * Created by iig on 11/6/15.
 */

object Waiter {
  case class ServeDrink(drink: Drink)
  case class DrinkServed(drink: Drink)
  case class Complaint(drink: Drink)
  case class FrustratedException(drink: Drink, guest: ActorRef) extends IllegalStateException("Too many complaints!")

  def props(hakkyHour: ActorRef, maxComplaintCount: Int) =
    Props(new Waiter(hakkyHour, maxComplaintCount))
}

class Waiter(hakkyHour: ActorRef, maxComplaintCount: Int) extends Actor {
  var complaintCount = 0
  override def receive = {
    case ServeDrink(drink) =>
      hakkyHour ! ApproveDrink(drink, sender())
    case DrinkPrepared(drink, guest) =>
      guest ! DrinkServed(drink)
    case Complaint(drink) =>
      complaintCount += 1
      if (complaintCount > maxComplaintCount)
        throw new FrustratedException(drink, sender)
  }
}
