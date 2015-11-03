package com.typesafe.training.hakkyhour

import akka.actor.{ Props, ActorLogging, Actor }

/**
 * Created by iig on 11/3/15.
 */

object HakkyHour {
  def props = Props(new HakkyHour())
}

class HakkyHour extends Actor with ActorLogging {
  log.debug("{} has opened!", "Hakky Hour")

  def receive: Receive = {
    case _ => log.info("Welcome to Hakky Hour!")
  }
}
