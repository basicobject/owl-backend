package owl.gateway

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.ws.TextMessage
import akka.http.scaladsl.testkit.{ScalatestRouteTest, WSProbe}
import akka.stream.scaladsl.{Sink, Source}
import akka.testkit.TestProbe
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import akka.pattern.pipe
import org.scalatest.concurrent.ScalaFutures
import owl.common.NickCmd
import owl.common.protocol.{BadCmd, ExitCmd, JoinCmd, ListCmd, MsgCmd, NickCmd}
import owl.session.Session

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

class GatewayServerSpec
    extends AnyWordSpec
    with Matchers
    with ScalatestRouteTest
    with ScalaFutures {
  import GatewayServer._

  "Gateway service" should {
    "respond to ping requests" in {
      Get("/ping") ~> pingRoute ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[String] shouldEqual ("PONG")
      }
    }

    "upgrade to websockets on /ws/:id route" in {
      val wsClient = WSProbe()
      WS("/ws/12345", wsClient.flow) ~> wsRoute ~> check {
        isWebSocketUpgrade shouldEqual true
      }
    }

    "test websocket completion" in {
      val wsClient = WSProbe()
      WS("/ws/12345", wsClient.flow) ~> wsRoute ~> check {
        wsClient.sendMessage("/list")
        wsClient.expectMessage("/ACK")
        // I am not sure why we need to add the above lines in this test but without that the test fails
        // after completion you won't be able to send any message
        wsClient.sendCompletion()
        wsClient.expectCompletion()
      }
    }

    "expect acknowledgement for messages" in {
      val wsClient = WSProbe()
      WS("/ws/12345", wsClient.flow) ~> wsRoute ~> check {
        wsClient.sendMessage("/list")
        wsClient.expectMessage("/ACK")
      }
    }

    "create a session on connecting to websocket" in {
      val wsClient = WSProbe()
      WS("/ws/12345", wsClient.flow) ~> wsRoute ~> check {
        sessionMapper.getSession("12345").futureValue shouldEqual Some(
          Session("12345", Host, Port)
        )
      }
    }

    "failureFLow should send the failure response" in {
      val future = Source
        .single(TextMessage("Some message"))
        .via(failureFlow)
        .runWith(Sink.seq)
      val result = Await.result(future, 3.seconds)
      result.head shouldEqual TextMessage.Strict("Failed")
    }

    "ackFlow should respond with an ACK for supported messages" in {
      val probe = TestProbe()

      Source(Seq(ListCmd, BadCmd))
        .via(ackFlow)
        .runWith(Sink.seq)
        .pipeTo(probe.ref)

      probe.expectMsg(
        3.seconds,
        Seq(TextMessage.Strict("/ACK"), TextMessage.Strict("/Unsupported"))
      )
    }

    "parseMessageFlow shoud parse an input message" in {
      val probe = TestProbe()

      val messages = Seq(
        "/exit",
        "/list",
        "/nick tiger",
        "/msg @roger I am going to the party",
        "/join #geeklife",
        "/idk",
        "/idk what I am doing",
        "idk what I am doing"
      ).map(TextMessage(_))

      Source(messages).via(parseMessage).runWith(Sink.seq).pipeTo(probe.ref)
      probe.expectMsg(
        3.seconds,
        Seq(
          ExitCmd,
          ListCmd,
          NickCmd("tiger"),
          MsgCmd("roger", "I am going to the party"),
          JoinCmd("geeklife"),
          BadCmd,
          BadCmd,
          BadCmd
        )
      )
    }
  }
}
