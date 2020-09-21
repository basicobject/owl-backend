package messaging

import java.util.UUID

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

case class ServerMessage[T <: GenericMessage](event: String, body: T)
case class ClientMessage[T <: GenericMessage](userId: Long, body: T)
