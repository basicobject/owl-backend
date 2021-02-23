import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.scaladsl.Flow
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.ExecutionContext
import scala.io.StdIn
import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import messaging._

import scala.annotation.tailrec

object WebServer extends App with LazyLogging {

  final val service = "owl-messaging"
  final val Port = 1338
  final val Host = "localhost"
  final val Ping = "ping"
  final val Pong = "PONG"
  final val Ws = "ws"
  final val WelcomeMessage = s"Welcome to $service"
  final val StartupMessage =
    s"$service is running at $Port ! Stop the server by pressing q"
  final val ShutdownMessage = s"Bye .. $service is shutting down"

  implicit val actorSystem: ActorSystem = ActorSystem(s"$service-actor-system")
  implicit val ec: ExecutionContext = actorSystem.dispatcher

  def helloRoute: Route =
    pathEndOrSingleSlash {
      complete(WelcomeMessage)
    }

  def pingRoute: Route =
    path(Ping) {
      complete(Pong)
    }

  def wsRoute: Route =
    path(Ws) {
      handleWebSocketMessages {
        Flow[Message].collect {
          case TextMessage.Strict(text) => TextMessageHandler.handle(text.trim)
          case _                        => TextMessageHandler.UnsupportedMessageFormatResponse
        }
      }
    }

  def run(): Unit = {
    logger.info(StartupMessage)
    val server = Http()
      .newServerAt(Host, Port)
      .bind(helloRoute ~ pingRoute ~ wsRoute)

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
