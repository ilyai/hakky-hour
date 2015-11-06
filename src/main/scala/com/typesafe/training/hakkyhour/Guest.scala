package com.typesafe.training.hakkyhour

import akka.actor.{ Props, Actor }

/**
 * Created by iig on 11/6/15.
 */

object Guest {
  def props = Props(new Guest)
}

class Guest extends Actor {
  override def receive =
    Actor.emptyBehavior
}
