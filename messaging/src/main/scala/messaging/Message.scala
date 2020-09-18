package messaging

import messaging.SessionMap.Session

sealed trait GenericMessage

case object EmptyMessage extends GenericMessage
case object PongMessage extends GenericMessage
case class PlainMessage(text: String) extends GenericMessage
case class SessionInfo(sessions: Seq[Session]) extends GenericMessage

case class ServerMessage(event: String, body: GenericMessage)

object ServerEvent extends Enumeration {
  type Event = Value
  val ERROR, PONG, PLAIN, NOT_FOUND, JOIN = Value
}
