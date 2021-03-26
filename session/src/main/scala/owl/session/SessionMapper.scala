package owl.session

import com.typesafe.scalalogging.LazyLogging
import io.grpc.{Server, ServerBuilder}
import owl.common.OwlService
import owl.proto.session.SessionServiceGrpc

import scala.concurrent.ExecutionContext

object SessionMapper extends OwlService with LazyLogging {
  override final val service = "session"

  type GatewayService = (Host, Port)

  /**
    * This could be replaced with a distributed cache
    */
  var sessionMap: Map[String, GatewayService] =
    Map.empty[String, GatewayService]

  val server: Server = ServerBuilder
    .forPort(50001)
    .addService(
      SessionServiceGrpc
        .bindService(new SessionServiceImpl(), ExecutionContext.global)
    )
    .build()

  sys.addShutdownHook {
    server.shutdown()
  }

  override def run(): Unit = {
    server.start()
    server.awaitTermination()
  }
}
