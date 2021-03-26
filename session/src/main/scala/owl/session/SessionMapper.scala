package owl.session

import com.typesafe.scalalogging.LazyLogging
import io.grpc.{Server, ServerBuilder}
import owl.common.OwlService
import owl.proto.session.SessionServiceGrpc

import scala.annotation.tailrec
import scala.concurrent.{ExecutionContext, Future}
import scala.io.StdIn

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
    logger.info(s"[Shutdown Hook] $service")
    server.shutdown()
  }

  def startGrpcServer(): Future[Unit] =
    Future {
      server.start()
      server.awaitTermination()
    }(ExecutionContext.global)

  override def run(): Unit = {
    startGrpcServer()

    @tailrec
    def handleKeypress(): Unit =
      if (StdIn.readChar() == 'q') {
        logger.info(s"Quitting $service")
        System.exit(0)
      } else handleKeypress()

    handleKeypress()
  }
}
