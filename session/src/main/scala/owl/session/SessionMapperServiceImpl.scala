package owl.session

import akka.actor.typed.ActorSystem

import scala.concurrent.Future

class SessionMapperServiceImpl(system: ActorSystem[_])
    extends SessionMapperService {
  implicit val sys = system
  override def createSession(
      request: CreateSessionRequest
  ): Future[CreateSessionReply] = {
    Future.successful(CreateSessionReply(true))
  }
}
