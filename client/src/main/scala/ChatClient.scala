import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.http.scaladsl.model.ws.{
  Message,
  TextMessage,
  WebSocketRequest,
  WebSocketUpgradeResponse
}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.stream.IOResult
import akka.stream.scaladsl.{Flow, Keep, Sink, Source, StreamConverters}
import akka.util.ByteString

import scala.concurrent.{ExecutionContext, Future}

object ChatClient extends App {
  implicit val as: ActorSystem = ActorSystem("client")
  implicit val ec: ExecutionContext = as.dispatcher

  val clientSource: Source[ByteString, Future[IOResult]] =
    StreamConverters.fromInputStream(() => System.in)

  val incoming: Sink[String, Future[Done]] =
    Sink.foreach(println)

  val incomingToByteString: Flow[Message, String, NotUsed] =
    Flow[Message].collect {
      case message: TextMessage.Strict => ByteString(message.text).toString()
      case _                           => throw new RuntimeException(s"Incorrect messages")
    }

  val toTextMessage: Flow[ByteString, TextMessage.Strict, NotUsed] =
    Flow[ByteString].collect {
      case str: ByteString => TextMessage.Strict(str.toString())
      case _               => throw new RuntimeException(s"Incorrect byte sequence")
    }

  sys.addShutdownHook {
    as.terminate()
  }

  val webSocketFlow: Flow[Message, Message, Future[WebSocketUpgradeResponse]] =
    Http().webSocketClientFlow(
      WebSocketRequest.fromTargetUriString("ws://localhost:1338/ws/nobody")
    )

  val (upgradeResponse, closed): (
      Future[WebSocketUpgradeResponse],
      Future[Done]
  ) =
    clientSource
      .via(toTextMessage)
      .viaMat(webSocketFlow)(Keep.right)
      .via(incomingToByteString)
      .toMat(incoming)(Keep.both)
      .run()

  val connected = upgradeResponse.flatMap { upgrade =>
    if (upgrade.response.status == StatusCodes.SwitchingProtocols) {
      Future.successful(Done)
    } else
      throw new RuntimeException(
        s"Connection failed: ${upgrade.response.status}"
      )
  }

  connected.onComplete(message => println("onComplete " + message))
  closed.foreach(_ => println("closed"))
}
