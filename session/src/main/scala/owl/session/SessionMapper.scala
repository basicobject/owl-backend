package owl.session

import com.typesafe.scalalogging.LazyLogging
import io.grpc.{Server, ServerBuilder}
import owl.common.{GatewayService, OwlService}
import owl.common.session.SessionServiceGrpc

import scala.annotation.tailrec
import scala.concurrent.{ExecutionContext, Future}
import scala.io.StdIn

object SessionMapper extends OwlService with LazyLogging {
  override final val serviceName = "session"

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
    logger.info(s"[Shutdown Hook] $serviceName")
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
        logger.info(s"Quitting $serviceName")
        System.exit(0)
      } else handleKeypress()

    handleKeypress()
  }
}
