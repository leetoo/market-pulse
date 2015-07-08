package actors

import actors.ExchangeAccountant.{TotalExchange, TotalExchangeRequired}
import actors.ExchangeStatsAnnouncer.{AnnouncementRequest, ExchangeStats}
import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import akka.util.Timeout
import org.junit.runner.RunWith
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfterAll, GivenWhenThen, MustMatchers, WordSpecLike}

import scala.concurrent.duration._
import scala.language.postfixOps

@RunWith(classOf[JUnitRunner])
class ExchangeStatsAnnouncerSpec extends TestKit(ActorSystem("ExchangeStatsAnnouncerSystem"))
                          with ImplicitSender
                          with WordSpecLike
                          with BeforeAndAfterAll
                          with MustMatchers
                          with ScalaFutures
                          with GivenWhenThen {

  sealed trait Fixtures {
    lazy val exchange = ("EUR", "PLN")

    lazy val totalExchange = TotalExchange(exchange, 25.0, 100.0)
    lazy val exchangeStats = ExchangeStats(exchange._1, exchange._2, totalExchange.totalSale, totalExchange.totalBuy)
  }

  sealed trait Actors extends Fixtures {
    implicit val timeout = Timeout(2 seconds)

    val out = TestProbe()
    val exchangeStatsAnnouncer = system.actorOf(ExchangeStatsAnnouncer.props(out.ref))
  }

  override def afterAll() {
    system.shutdown()
  }

  "ExchangeStatsAnnouncer" should {
    "publish exchange stats from the event stream" in new Actors {
      When("a total exchange event is published")
      system.eventStream.publish(totalExchange)

      Then("the output should receive valid exchange stats")
      out.expectMsgAllClassOf(classOf[ExchangeStats])
    }

    "request current exchange stats when asked explicitly" in new Actors {
      Given("a stats provider is present")
      val statsProvider = TestProbe()
      system.eventStream.subscribe(statsProvider.ref, classOf[TotalExchangeRequired])

      When("a an announcement request sent")
      exchangeStatsAnnouncer ! AnnouncementRequest("get")

      Then("the stats provider should receive the message")
      statsProvider.expectMsg(TotalExchangeRequired(exchangeStatsAnnouncer))
    }

  }

}
