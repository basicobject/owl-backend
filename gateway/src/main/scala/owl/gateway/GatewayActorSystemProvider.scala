package owl.gateway

import akka.actor.typed.ActorSystem
import owl.gateway.Gateway.GatewayAction
import owl.gateway.GatewayService.serviceName

object GatewayActorSystemProvider {
  private val as =
    ActorSystem[GatewayAction](Gateway(), s"$serviceName-actor-system")

  val get: ActorSystem[GatewayAction] = as
}
