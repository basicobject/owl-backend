package owl.session

import io.grpc.ManagedChannelBuilder
import owl.common.session.{CreateSessionRequest, SessionServiceGrpc}

object Client extends App {
  val channel =
    ManagedChannelBuilder
      .forAddress("localhost", 50001)
      .usePlaintext()
      .build()
  val syncClient = SessionServiceGrpc.blockingStub(channel)
  val response =
    syncClient.createSession(CreateSessionRequest("deepak", 2000, "localhost"))

  println(s"Response from GRPC Server ${response.success}")
  channel.shutdown()
}
