package controllers

import actors.Receptionist.{Trade, TradeAccepted}
import akka.actor.ActorSystem
import akka.testkit.{TestKit, TestProbe}
import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit.JUnitRunner
import play.api.http.HeaderNames
import play.api.libs.json._
import play.api.mvc.{Controller, _}
import play.api.test.Helpers._
import play.api.test._

@RunWith(classOf[JUnitRunner])
class TradeControllerSpec extends TestKit(ActorSystem("TradeControllerTestSystem"))
                             with WordSpecLike
                             with Results
                             with BeforeAndAfterAll
                             with MustMatchers {

  override def afterAll() {
    system.shutdown()
  }

  sealed trait Fixtures {
      implicit val tradeWrites = Json.writes[Trade]

      lazy val validTrade = Trade("134256", "EUR", "GBP", BigDecimal.valueOf(1000L), BigDecimal.valueOf(747.10), BigDecimal.valueOf(0.7471), "24-JAN-15 10:27:44", "FR")
      lazy val validTradeAsJson = Json.toJson(validTrade)

      lazy val incompleteJson = JsObject(Nil)
  }

  sealed trait ActorProbes {
    lazy val receptionistProbe = TestProbe()
  }

  sealed trait WebComponents extends ActorProbes {

    class TestController(probe: TestProbe) extends Controller with TradeController {
      override val receptionist = probe.ref
    }

    lazy val controller = new TestController(receptionistProbe)
  }

  "TradeController" should {
    "register a currency trade" in new WithApplication with WebComponents with Fixtures {
      val request = FakeRequest(POST, "/api/trades").withHeaders(HeaderNames.CONTENT_TYPE -> "application/json").withBody(validTradeAsJson)
      val response = controller.create.apply(request)

      receptionistProbe.expectMsg(validTrade)
      receptionistProbe.reply(TradeAccepted)

      status(response) mustEqual (OK)
      contentType(response) mustEqual Some("application/json")
    }

    "set bad request status on invalid input format" in new WithApplication with WebComponents with Fixtures {
      val request = FakeRequest(POST, "/api/trades").withHeaders(HeaderNames.CONTENT_TYPE -> "application/json").withBody(incompleteJson)
      val response = controller.create.apply(request)

      receptionistProbe.expectNoMsg()

      status(response) mustEqual(BAD_REQUEST)
      contentType(response) mustEqual Some("application/json")
    }

  }
}