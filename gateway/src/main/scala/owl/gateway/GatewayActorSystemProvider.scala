package owl.gateway

import akka.actor.typed.ActorSystem
import owl.gateway.Gateway.GatewayAction
import owl.gateway.GatewayServer.service

object GatewayActorSystemProvider {
  private val as =
    ActorSystem[GatewayAction](Gateway(), s"$service-actor-system")

  val get: ActorSystem[GatewayAction] = as
}
