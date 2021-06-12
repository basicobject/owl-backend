package owl.gateway

import akka.actor.FSM.Failure
import com.typesafe.scalalogging.LazyLogging
import io.grpc.{ManagedChannel, ManagedChannelBuilder}
import owl.common.session.{CreateSessionRequest, SessionServiceGrpc}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal

object SessionClient extends LazyLogging {
  val channel: ManagedChannel =
    ManagedChannelBuilder
      .forAddress("localhost", 50001)
      .usePlaintext()
      .build()

  val syncClient = SessionServiceGrpc.stub(channel)

  def register(userId: String, host: String, port: Int): Future[Boolean] = {
    syncClient
      .createSession(CreateSessionRequest(userId, port, host))
      .map { response =>
        logger.info(s"[Session Mapper Response] ${response.success}")
        response.success
      }
      .recoverWith {
        case NonFatal(_) => Future.successful(false)
      }
  }
}
