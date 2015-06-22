package actors

import actors.Common.Amount
import actors.ExchangeAccountant.{Sale, SaleAccepted, TotalExchange}
import actors.ExchangeStatsAnnouncer.ExchangeStats
import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import akka.util.Timeout
import org.junit.runner.RunWith
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.junit.JUnitRunner
import org.scalatest.{GivenWhenThen, BeforeAndAfterAll, MustMatchers, WordSpecLike}

import scala.concurrent.duration._

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

    lazy val out = TestProbe()
    lazy val exchangeStatsAnnouncer = system.actorOf(ExchangeStatsAnnouncer.props(out.ref))
  }

  override def afterAll() {
    system.shutdown()
  }

  "ExchangeStatsAnnouncer" should {
    "publish exchange stats from the event stream" in new Actors {
      Given("the stats announcer was initialized")
      exchangeStatsAnnouncer

      When("a total exchange event is published")
      system.eventStream.publish(totalExchange)

      Then("the output should receive valid exchange stats")
      out.expectMsg(exchangeStats)
    }
  }

}
