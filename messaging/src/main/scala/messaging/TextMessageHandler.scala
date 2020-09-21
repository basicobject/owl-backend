package messaging

import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import akka.http.scaladsl.model.ws.TextMessage

object TextMessageHandler {
  def handle(text: String): TextMessage = text match {
    case s"/JOIN $nickname" => joinHandler(nickname)
    case s"/PING $sessonId" => pingHandler(sessonId.toLong)
    case _                  => handleClientMessage(text)
  }

  private def authenticate(sessionId: Long)(fn: Session => TextMessage) =
    SessionManager.authenticate(sessionId) match {
      case Some(session) => fn(session)
      case None          => UnauthorisedResponse
    }

  val UnsupportedMessageFormatResponse: TextMessage = TextMessage(
    ServerMessage(
      ServerEvent.ERROR.toString,
      PlainMessage("Unsupported message format")
    ).asJson.noSpaces
  )

  val UnauthorisedResponse: TextMessage = TextMessage(
    ServerMessage(ServerEvent.UNAUTHORISED.toString, EmptyMessage).asJson.noSpaces
  )

  val PongResponse: TextMessage = TextMessage(
    ServerMessage(ServerEvent.PONG.toString, PongMessage).asJson.noSpaces
  )

  private def joinHandler(nickname: String): TextMessage = {
    SessionManager.findOrCreate(nickname)
    TextMessage(
      ServerMessage(
        ServerEvent.JOIN.toString,
        SessionInfo(SessionManager.getSessions)
      ).asJson.noSpaces
    )
  }

  private def pingHandler(sessionId: Long): TextMessage =
    authenticate(sessionId) { session =>
      SessionManager.update(session.copy(state = Session.ACTIVE))
      PongResponse
    }

  private def handleClientMessage(clientMessage: String): TextMessage = {
    decode[ClientMessage[NewMessage]](clientMessage) match {
      case Right(message) =>
        authenticate(message.userId) { _session =>
          TextMessage(
            ServerMessage(
              ServerEvent.ACK.toString,
              AckMessage(message.body.messageId)
            ).asJson.noSpaces
          )
        }
      case Left(e) =>
        TextMessage(
          ServerMessage(ServerEvent.ERROR.toString, PlainMessage(e.getMessage)).asJson.noSpaces
        )
    }
  }
}
