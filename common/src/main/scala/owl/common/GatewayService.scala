package owl.common

/**
  * GatewayService is the websocket server to which the users are
  * getting connected during their session.
  * @param host Hostname to which users are connected
  * @param port Port number
  */
final case class GatewayService(host: String, port: Int)
