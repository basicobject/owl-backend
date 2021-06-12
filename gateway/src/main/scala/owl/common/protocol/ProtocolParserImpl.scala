package owl.common.protocol

class ProtocolParserImpl extends ProtocolParser {
  override def parse(message: String): Either[String, Protocol] = {
    val command = message match {
      case "/list"               => ListCmd
      case "/exit"               => ExitCmd
      case s"/msg @$to $message" => MsgCmd(to, message)
      case s"/join #$channel"    => JoinCmd(channel)
      case s"/nick $nickname"    => NickCmd(nickname)
      case _                     => BadCmd
    }

    command match {
      case BadCmd => Left(message)
      case _      => Right(command)
    }
  }
}
