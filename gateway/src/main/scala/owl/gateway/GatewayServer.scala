package owl.gateway

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl.Flow
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging

import scala.annotation.tailrec
import scala.concurrent.ExecutionContext
import scala.io.StdIn

object GatewayServer extends App with LazyLogging {
  final val config = ConfigFactory.load()
  final val service = "gateway"
  final val Port = config.getInt("port")
  final val Host = config.getString("host")
  final val Ping = config.getString("pingRoute")
  final val Pong = "PONG"
  final val Ws = config.getString("wsRoute")
  final val WelcomeMessage = s"Welcome to $service"
  final val StartupMessage =
    s"$service is running at $Port ! Stop the server by pressing q"
  final val ShutdownMessage = s"Bye .. $service is shutting down"

  implicit val actorSystem: ActorSystem[Nothing] =
    ActorSystem[Nothing](Behaviors.empty, s"$service-actor-system")
  implicit val ec: ExecutionContext = actorSystem.executionContext

  def slashOrEmpty: Route =
    pathEndOrSingleSlash {
      reject
    }

  def pingRoute: Route =
    path(Ping) {
      complete(Pong)
    }

  def wsRoute: Route =
    path(Ws) {
      handleWebSocketMessages {
        Flow[Message].collect {
          case TextMessage.Strict(text) => TextMessage("Gateway is up")
          case _                        => TextMessage("Unsupported message")
        }
      }
    }

  def run(): Unit = {
    logger.info(StartupMessage)
    val server = Http()
      .newServerAt(Host, Port)
      .bind(slashOrEmpty ~ pingRoute ~ wsRoute)

    @tailrec
    def handleKeypress(): Unit =
      if (StdIn.readChar() == 'q') {
        server
          .flatMap(_.unbind())
          .onComplete(_ => actorSystem.terminate())
        logger.info(ShutdownMessage)
      } else handleKeypress()

    handleKeypress()
  }

  /**
    * Start the server
    */
  run()
}
