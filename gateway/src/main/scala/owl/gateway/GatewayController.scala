package owl.gateway

import akka.NotUsed
import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl.Flow
import com.typesafe.scalalogging.LazyLogging
import owl.gateway.Gateway.GatewayAction
import owl.gateway.GatewayService.{Host, Port}
import owl.session.Session

object GatewayController extends LazyLogging {

  import owl.gateway.Implicits.ActorSystem

  final val Pong = "PONG"
  final val PingPath = "ping"
  final val WsPath = "ws"
  final val Unsupported = TextMessage.Strict("/unsupported")
  final val Ack = TextMessage.Strict("/ack")

  val slashOrEmpty: Route =
    pathEndOrSingleSlash {
      reject
    }

  val pingRoute: Route =
    path(PingPath) {
      complete(Pong)
    }

  val wsRoute: Route =
    pathPrefix(WsPath) {
      path(Segment) { userId =>
        registerWithSessionService(userId)
        handleWebSocketMessages(wsFlow)
      }
    }

  val routes: Route = slashOrEmpty ~ pingRoute ~ wsRoute

  def registerWithSessionService(
      userId: String
  )(implicit as: ActorSystem[GatewayAction]): Unit = {
    logger.info(s"Registering session for $userId, $Host, $Port")
    as ! Gateway.RegisterSession(Session(userId, Host, Port))
  }

  def handleIncomingMessage(
      text: String
  )(implicit as: ActorSystem[GatewayAction]): Unit = {
    logger.info(s"Handle NewMessage: $text")
    as ! Gateway.MessagingHandler(text)
  }

  val wsFlow: Flow[Message, TextMessage.Strict, NotUsed] =
    Flow[Message].collect {
      case TextMessage.Strict(text) =>
        handleIncomingMessage(text)
        Ack
      case _ => Unsupported
    }

  // For testing purpose compatible with wsFlow
  val echoFlow: Flow[Message, TextMessage.Strict, NotUsed] =
    Flow[Message].collect {
      case TextMessage.Strict(text) => TextMessage(text)
      case _                        => Unsupported
    }
}
