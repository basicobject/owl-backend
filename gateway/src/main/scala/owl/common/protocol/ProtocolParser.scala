package owl.common.protocol

trait ProtocolParser {
  def parse(message: String): Either[String, Protocol]
}
