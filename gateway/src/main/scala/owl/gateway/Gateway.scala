package owl.gateway

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import owl.session.{Session, SessionMapper}

object Gateway {
  trait GatewayAction
  case class MessagingHandler(text: String) extends GatewayAction
  case class RegisterSession(session: Session) extends GatewayAction

  val sessionMapper = new SessionMapper()

  def apply(): Behavior[GatewayAction] =
    Behaviors.setup { context =>
      val messageHandler =
        context.spawn(IncomingMessageHandler(), "incoming-message-handler")

      Behaviors.receiveMessage {
        case MessagingHandler(text) =>
          context.log.info(text)
          messageHandler ! text
          Behaviors.same
        case RegisterSession(session) =>
          sessionMapper.addSession(session)
          Behaviors.same
      }
    }
}
