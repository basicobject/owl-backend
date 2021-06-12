package owl.common.protocol

sealed trait Protocol
case object ListCmd extends Protocol
case object ExitCmd extends Protocol
case object BadCmd extends Protocol
case class JoinCmd(channel: String) extends Protocol
case class MsgCmd(to: String, text: String) extends Protocol
case class NickCmd(name: String) extends Protocol
