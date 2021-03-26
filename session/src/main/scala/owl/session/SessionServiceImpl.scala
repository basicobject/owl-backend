package owl.session;

import owl.proto.session.{
  CreateSessionRequest,
  CreateSessionResponse,
  SessionServiceGrpc
}
import owl.session.SessionMapper.GatewayService

import scala.concurrent.Future

class SessionServiceImpl extends SessionServiceGrpc.SessionService {
  override def createSession(
      request: CreateSessionRequest
  ): Future[CreateSessionResponse] = {
    val gateWay: GatewayService = (request.host, request.port)
    SessionMapper.sessionMap += (request.userId -> gateWay)
    Future.successful(CreateSessionResponse(true))
  }

  private def findSession(userId: String): Future[Option[GatewayService]] = {
    Future.successful(SessionMapper.sessionMap.get(userId))
  }
}
