package com.typesafe.training.hakkyhour

import akka.actor.{ Props, ActorLogging, Actor }
import com.typesafe.training.hakkyhour.HakkyHour.CreateGuest

/**
 * Created by iig on 11/3/15.
 */

object HakkyHour {
  case object CreateGuest

  def props = Props(new HakkyHour())
}

class HakkyHour extends Actor with ActorLogging {
  log.debug("{} has opened!", "Hakky Hour")

  def receive: Receive = {
    case CreateGuest => createGuest()
  }

  def createGuest() =
    context.actorOf(Guest.props)
}
