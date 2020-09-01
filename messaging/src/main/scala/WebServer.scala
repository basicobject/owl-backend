import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.scaladsl.Flow
import com.typesafe.scalalogging.LazyLogging
import scala.concurrent.ExecutionContext
import scala.io.StdIn

object WebServer extends App with LazyLogging {

  val service = "Owl Messaging"

  implicit val actorSystem: ActorSystem = ActorSystem(
    "owl-messaging-actor-system"
  )
  implicit val ec: ExecutionContext = actorSystem.dispatcher

  def helloRoute: Route = pathEndOrSingleSlash {
    complete(s"Welcome to $service service")
  }

  def pingRoute: Route = path("ping") {
    complete("PONG")
  }

  def affirmRoute: Route = path("affirm") {
    handleWebSocketMessages(Flow[Message].collect {
      case TextMessage.Strict(text) => TextMessage(s"You said: $text")
    })
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
