package actors

import actors.Common.Exchange
import actors.ExchangeAccountant.{Sale, SaleAccepted}
import actors.Receptionist._
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.testkit.{TestKit, TestProbe}
import akka.util.Timeout
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, MustMatchers, WordSpecLike}

import scala.concurrent.duration._

class ReceptionistSpec extends TestKit(ActorSystem("ReceptionistTestSystem"))
                          with WordSpecLike
                          with BeforeAndAfterAll
                          with MustMatchers
                          with ScalaFutures {

  sealed trait Fixtures {
    lazy val tradeFromEur = Trade("134256", "EUR", "GBP", 1000, 747.10, 0.7471, "24-JAN-15 10:27:44", "FR")
    lazy val tradeFromPln = Trade("134256", "PLN", "EUR", 10000, 41500.00, 4.15, "24-JAN-15 10:27:46", "FR")

  }

  sealed trait Actors extends Fixtures {
    implicit val timeout = Timeout(2 seconds)

    lazy val accountantProbes = Map[Exchange, TestProbe](tradeFromEur.exchange -> new TestProbe(system),
                                                         tradeFromPln.exchange -> new TestProbe(system))

    lazy val receptionist = system.actorOf(Props(new Receptionist() {
      override def newAccountant(exchange: Exchange): ActorRef = accountantProbes.get(exchange).get.ref
    }))
  }

  override def afterAll() {
    system.shutdown()
  }

  "Receptionist" should {
    "register trades" in new Actors {

      def register(trade: Trade, expectedId: Int) = {
        val result = (receptionist ? trade)

        accountantProbes(trade.exchange).expectMsg(Sale(expectedId, trade.amountSell, trade.amountBuy))
        accountantProbes(trade.exchange).reply(SaleAccepted(expectedId))

        whenReady(result) { answer =>
          answer must be(TradeAccepted)
        }
      }

      register(tradeFromEur, 0)
      register(tradeFromEur, 1)
      register(tradeFromEur, 2)

      register(tradeFromPln, 0)
      register(tradeFromPln, 1)
    }

    "repeat trades that were not delivered" in new Actors {
      val trade = tradeFromEur
      val result = (receptionist ? trade)

      val sale = Sale(0, trade.amountSell, trade.amountBuy)
      accountantProbes(trade.exchange).expectMsg(200 milliseconds, sale)
      accountantProbes(trade.exchange).expectMsg(200 milliseconds, sale)
      accountantProbes(trade.exchange).expectMsg(200 milliseconds, sale)
      accountantProbes(trade.exchange).reply(SaleAccepted(sale.id))

      accountantProbes(trade.exchange).expectNoMsg(500 milliseconds)
    }

  }

}
