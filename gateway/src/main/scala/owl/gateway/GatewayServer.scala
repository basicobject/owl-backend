package owl.gateway

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import owl.common.OwlService
import owl.gateway.Gateway.GatewayAction

import scala.annotation.tailrec
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.io.StdIn
import scala.util.{Failure, Success}

object GatewayServer extends OwlService with LazyLogging {
  override final val service = "gateway"

  final val config = ConfigFactory.load()
  final val Port = config.getInt("port")
  final val Host = config.getString("host")
  final val WelcomeMessage = s"Welcome to $service"
  final val StartupMessage =
    s"$service is running at $Port ! Stop the server by pressing q"
  final val ShutdownMessage = s"Bye .. $service is shutting down"

  implicit val as: ActorSystem[GatewayAction] = GatewayActorSystemProvider.get
  implicit val ec: ExecutionContext = GatewayExecutionContextProvider.get

  override def run(): Unit = {
    val server = Http()
      .newServerAt(Host, Port)
      .bind(GatewayController.routes)
      .map(_.addToCoordinatedShutdown(hardTerminationDeadline = 10.seconds))

    server.onComplete {
      case Success(_binding) =>
        logger.info(StartupMessage)
      case Failure(exception) =>
        logger.error(s"Failed to bind $service, terminating", exception)
        as.terminate()
    }

    def stop(): Unit =
      server.flatMap(_.unbind()).onComplete(_ => as.terminate())

    @tailrec
    def handleKeypress(): Unit =
      if (StdIn.readChar() == 'q') {
        stop()
        logger.info(ShutdownMessage)
      } else handleKeypress()

    handleKeypress()
  }
}
