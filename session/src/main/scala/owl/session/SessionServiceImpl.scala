package owl.session;

import owl.common.GatewayService
import owl.common.session.{
  CreateSessionRequest,
  CreateSessionResponse,
  SessionServiceGrpc
}

import scala.concurrent.Future

class SessionServiceImpl extends SessionServiceGrpc.SessionService {
  override def createSession(
      request: CreateSessionRequest
  ): Future[CreateSessionResponse] = {
    val gateway = GatewayService(request.host, request.port)
    SessionMapper.sessionMap += (request.userId -> gateway)
    Future.successful(CreateSessionResponse(true))
  }

  private def findSession(userId: String): Future[Option[GatewayService]] = {
    Future.successful(SessionMapper.sessionMap.get(userId))
  }
}
