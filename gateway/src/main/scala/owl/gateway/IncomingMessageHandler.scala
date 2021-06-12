package owl.gateway

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import owl.common.protocol.{BadCmd, Protocol, ProtocolParserImpl}

object IncomingMessageHandler {
  def apply(): Behavior[String] =
    Behaviors.receiveMessage { message =>
      println("I got the message" + message)
      parseMessage(message)
      // forward message to messenger
      Behaviors.same
    }

  val parser = new ProtocolParserImpl()

  def parseMessage(text: String): Protocol = {
    parser.parse(text) match {
      case Right(protocol) => protocol
      case Left(error)     => BadCmd
    }
  }
}
