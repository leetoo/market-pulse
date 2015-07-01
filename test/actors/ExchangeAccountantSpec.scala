package actors

import actors.Common.Amount
import actors.ExchangeAccountant.{TotalExchangeRequired, TotalExchange, Sale, SaleAccepted}
import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import akka.util.Timeout
import org.junit.runner.RunWith
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfterAll, MustMatchers, WordSpecLike}

import scala.concurrent.duration._

@RunWith(classOf[JUnitRunner])
class ExchangeAccountantSpec extends TestKit(ActorSystem("ExchangeAccountantTestSystem"))
                          with ImplicitSender
                          with WordSpecLike
                          with BeforeAndAfterAll
                          with MustMatchers
                          with ScalaFutures {

  sealed trait Fixtures {
    lazy val exchange = ("EUR", "PLN")

    def sale(id: Int, amountSale: Amount = 1.0, amountBuy: Amount = 1.0) = Sale(id, amountSale, amountBuy)
  }

  sealed trait Actors extends Fixtures {
    implicit val timeout = Timeout(2 seconds)

    val probe = TestProbe()
    val exchangeAccountant = system.actorOf(Props(new ExchangeAccountant(exchange)))
  }

  override def afterAll() {
    system.shutdown()
  }

  "ExchangeAccountant" should {
    "keep track of incoming sales" in new Actors {
      exchangeAccountant.tell(sale(0), probe.ref)
      probe.expectMsg(SaleAccepted(0))
    }

    "ignore sales with future id" in new Actors {
      exchangeAccountant.tell(sale(1), probe.ref)
      probe.expectNoMsg()
    }

    "confirm already accepted sales" in new Actors {
      exchangeAccountant.tell(sale(0), probe.ref)
      probe.expectMsg(SaleAccepted(0))

      exchangeAccountant.tell(sale(0), probe.ref)
      probe.expectMsg(SaleAccepted(0))
    }

    "publish total exchange values to the event stream" in new Actors {
      val eventStreamListener = TestProbe()

      def sell(id: Int, amountSale: Amount, amountBuy: Amount) = {
        exchangeAccountant.tell(sale(id, amountSale, amountBuy), probe.ref)
        probe.expectMsg(SaleAccepted(id))
      }

      system.eventStream.subscribe(eventStreamListener.ref, classOf[TotalExchange])

      sell(0, 10.0, 5.0)
      eventStreamListener.expectMsg(TotalExchange(exchange, 10.0, 5.0))

      sell(1, 20.0, 10.0)
      eventStreamListener.expectMsg(TotalExchange(exchange, 30.0, 15.0))
    }

    "publish total exchange value when an explicit request is published to the event stream" in new Actors {
      val totalExchangeRequester = TestProbe()

      system.eventStream.publish(TotalExchangeRequired(totalExchangeRequester.ref))
      totalExchangeRequester.expectMsgAllClassOf(classOf[TotalExchange])
    }

  }

}
