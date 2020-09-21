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

object WebServer extends App with LazyLogging {

  final val service = "owl-messaging"

  implicit val actorSystem: ActorSystem = ActorSystem(s"$service-actor-system")
  implicit val ec: ExecutionContext = actorSystem.dispatcher

  def helloRoute: Route = pathEndOrSingleSlash {
    complete(s"Welcome to $service service")
  }

  def pingRoute: Route = path("ping") {
    complete("PONG")
  }

  def affirmRoute: Route = path("affirm") {
    handleWebSocketMessages {
      Flow[Message].collect {
        case TextMessage.Strict(text) => TextMessageHandler.handle(text.trim)
        case _                        => TextMessageHandler.UnsupportedMessageFormatResponse
      }
    }
  }

  val PORT = 1338

  val server = Http()
    .newServerAt("localhost", PORT)
    .bind(helloRoute ~ pingRoute ~ affirmRoute)

  logger.info(s"$service server is running at $PORT")

  StdIn.readLine()

  server.flatMap(_.unbind()).onComplete(_ => actorSystem.terminate())

  logger.info(s"Bye .. $service server is shutting down")
}
