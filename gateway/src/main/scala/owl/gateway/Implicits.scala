package owl.gateway

import akka.actor.typed.ActorSystem

import scala.concurrent.ExecutionContext

object Implicits {
  implicit val ActorSystem: ActorSystem[Gateway.GatewayAction] =
    GatewayActorSystemProvider.get
  implicit val ExecutionContext: ExecutionContext =
    GatewayExecutionContextProvider.get
}
