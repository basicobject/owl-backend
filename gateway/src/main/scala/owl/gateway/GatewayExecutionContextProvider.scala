package owl.gateway

import scala.concurrent.ExecutionContext

object GatewayExecutionContextProvider {
  val get: ExecutionContext = GatewayActorSystemProvider.get.executionContext
}
