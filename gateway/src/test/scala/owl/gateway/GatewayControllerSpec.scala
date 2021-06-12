package owl.gateway

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.{ScalatestRouteTest, WSProbe}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import owl.gateway.Gateway.sessionMapper
import owl.gateway.GatewayService.{Host, Port}
import owl.session.Session

class GatewayControllerSpec
    extends AnyWordSpec
    with Matchers
    with ScalaFutures
    with ScalatestRouteTest {
  import GatewayController._

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
      wsClient.expectMessage("/ack")
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
      wsClient.expectMessage("/ack")
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
}
