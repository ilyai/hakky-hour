/*
 * Copyright © 2014 Typesafe, Inc. All rights reserved.
 */

package com.typesafe.training.hakkyhour

import akka.actor._
import akka.event.Logging
import com.typesafe.training.hakkyhour.HakkyHour.{ GetStatus, CreateGuest }
import scala.annotation.tailrec
import scala.collection.breakOut
import scala.concurrent.duration.Duration
import scala.io.StdIn
import scala.concurrent.duration._
import akka.util.Timeout

import scala.util.{ Failure, Success }

object HakkyHourApp {

  val Opt = """(\S+)=(\S+)""".r

  def main(args: Array[String]): Unit = {
    //transfrom command line argument into Map
    val opts = argsToOpts(args.toList)
    //Akka set system properties ??
    applySystemProperties(opts)
    //get the name of the project from the command line else use default
    val name = opts.getOrElse("name", "hakky-hour")
    //Set up Akka actor system
    val system: ActorSystem = ActorSystem(s"$name-system")
    val hakkyHourApp = new HakkyHourApp(system, Settings(system).statusTimeout)
    hakkyHourApp.run()
  }

  def argsToOpts(args: Seq[String]): Map[String, String] =
    args.collect { case Opt(key, value) => key -> value }(breakOut)

  def applySystemProperties(opts: Map[String, String]): Unit =
    for ((key, value) <- opts if key startsWith "-D")
      System.setProperty(key substring 2, value)
}

class HakkyHourApp(system: ActorSystem, implicit val statusTimeout: Timeout) extends Terminal {
  import akka.pattern.ask
  import akka.pattern.pipe
  import system.dispatcher

  val log = Logging(system, getClass.getName)

  val hakkyHour = createHakkyHour()

  //  system.actorOf(Props(new Actor with ActorLogging {
  //    hakkyHour ! "Nice bar!"
  //    override def receive = {
  //      case msg: String => log.info(msg)
  //    }
  //  }))

  def run(): Unit = {
    log.warning(f"{} running%nEnter commands into the terminal, e.g. `q` or `quit`", getClass.getSimpleName)
    commandLoop()
    system.awaitTermination()
  }

  def createHakkyHour(): ActorRef =
    system.actorOf(HakkyHour.props(
      Settings(system).maxDrinkCount), "hakky-hour")

  @tailrec
  final def commandLoop(): Unit =
    //Call Command object function and pattern match the result
    Command(StdIn.readLine()) match {
      case Command.CreateGuest(count, drink, isStubborn, maxDrinkCount) =>
        createGuest(count, drink, isStubborn, maxDrinkCount)
        commandLoop()
      case Command.GetStatus =>
        getStatus()
        commandLoop()
      case Command.Quit =>
        system.shutdown()
      case Command.Unknown(command) =>
        log.warning("Unknown command {}!", command)
        commandLoop()
    }

  def createGuest(count: Int, drink: Drink, isStubborn: Boolean, maxDrinkCount: Int): Unit =
    for (i <- 1 to count)
      hakkyHour ! CreateGuest(drink, isStubborn, maxDrinkCount)

  def getStatus(): Unit =
    (hakkyHour ? HakkyHour.GetStatus).mapTo[HakkyHour.Status].onComplete {
      case Success(status) =>
        log.info("Number of guests: {}", status.guestCount)
      case Failure(error) =>
        log.error(error.toString)
    }
}
