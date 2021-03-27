package owl.gateway

import com.typesafe.scalalogging.LazyLogging
import io.grpc.{ManagedChannel, ManagedChannelBuilder}
import owl.common.session.{CreateSessionRequest, SessionServiceGrpc}

object SessionClient extends LazyLogging {
  val channel: ManagedChannel =
    ManagedChannelBuilder
      .forAddress("localhost", 50001)
      .usePlaintext()
      .build()

  val syncClient = SessionServiceGrpc.blockingStub(channel)

  def register(userId: String, host: String, port: Int): Unit = {
    val response =
      syncClient.createSession(CreateSessionRequest(userId, port, host))
    logger.info(s"[Session Mapper Response] ${response.success}")
  }
}
