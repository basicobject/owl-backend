package messaging

object ServerEvent extends Enumeration {
  type Event = Value
  val ERROR, PONG, PLAIN, NOT_FOUND, JOIN, ACK, UNAUTHORISED = Value
}
