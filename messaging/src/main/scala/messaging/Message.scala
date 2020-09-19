package messaging

import java.time.Instant
import java.util.UUID

import messaging.SessionMap.Session

sealed trait GenericMessage

case object EmptyMessage extends GenericMessage
case object PongMessage extends GenericMessage
case class PlainMessage(text: String) extends GenericMessage
case class SessionInfo(sessions: Seq[Session]) extends GenericMessage
case class AckMessage(messageId: UUID) extends GenericMessage

case class NewMessage(messageId: UUID,
                      userId: Long,
                      text: String,
                      createdAt: Long)
    extends GenericMessage

case class ServerMessage(event: String, body: GenericMessage)
case class ClientMessage[T <: GenericMessage](userId: Long, body: T)

object ServerEvent extends Enumeration {
  type Event = Value
  val ERROR, PONG, PLAIN, NOT_FOUND, JOIN, ACK = Value
}
