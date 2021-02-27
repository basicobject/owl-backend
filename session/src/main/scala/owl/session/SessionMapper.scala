package owl.session

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import owl.common.OwlService

import scala.annotation.tailrec
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}
import scala.io.StdIn
import scala.util.{Failure, Success}

object SessionMapper extends OwlService with LazyLogging {
  override final val service = "session"

  type GatewayService = (Host, Port)

  /**
    * This could be replaced with a distributed cache
    */
  private var sessionMap: Map[Long, GatewayService] =
    Map.empty[Long, GatewayService]

  def findSession(userId: Long): Future[Option[GatewayService]] = {
    Future.successful(sessionMap.get(userId))
  }

  def createSession(userId: Long, gateway: GatewayService): Future[Unit] = {
    sessionMap = sessionMap + (userId -> gateway)
    Future.successful(())
  }

  val conf =
    ConfigFactory
      .parseString("akka.http.server.preview.enable-http2 = on")
      .withFallback(ConfigFactory.defaultApplication())

  implicit val actorSystem =
    ActorSystem[Nothing](Behaviors.empty, s"$service-actor-system", conf)
  implicit val ec: ExecutionContext = actorSystem.executionContext

  val sessionMapperService: HttpRequest => Future[HttpResponse] =
    SessionMapperServiceHandler(new SessionMapperServiceImpl(actorSystem))

  val server =
    Http(actorSystem)
      .newServerAt("127.0.0.1", 1990)
      .bind(sessionMapperService)
      .map(_.addToCoordinatedShutdown(hardTerminationDeadline = 10.seconds))

  server.onComplete {
    case Success(binding) =>
      logger.info(s"Started gRPC server to ${binding.localAddress}")
    case Failure(exception) =>
      logger.error("Failed to start gRPC server", exception)
  }
  override def run(): Unit = {
    @tailrec
    def handleKeypress(): Unit =
      if (StdIn.readChar() == 'q') {
        server
          .flatMap(_.unbind())
          .onComplete(_ => actorSystem.terminate())
        logger.info("Shutting down")
      } else handleKeypress()

    handleKeypress()
  }
}
