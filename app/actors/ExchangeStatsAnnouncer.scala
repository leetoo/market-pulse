package actors

import actors.Common._
import actors.ExchangeAccountant.{TotalExchangeRequired, TotalExchange}
import actors.ExchangeStatsAnnouncer.{AnnouncementRequest, ExchangeStats}
import akka.actor.{Actor, ActorLogging, ActorRef, Props}

object ExchangeStatsAnnouncer {

  case class AnnouncementRequest(action: String)
  case class ExchangeStats(from: Currency, to: Currency, totalSales: Amount, totalBuys: Amount)

  def props(out: ActorRef) = Props(new ExchangeStatsAnnouncer(out))
}

class ExchangeStatsAnnouncer(out: ActorRef) extends Actor with ActorLogging {

  context.system.eventStream.subscribe(self, classOf[TotalExchange])

  override def postStop() = {
    context.system.eventStream.unsubscribe(self, classOf[TotalExchange])
  }

  def receive: Receive = {
    case AnnouncementRequest("get") => context.system.eventStream.publish(TotalExchangeRequired(self))
    case te: TotalExchange => out ! ExchangeStats(te.exchange._1, te.exchange._2, te.totalSale, te.totalBuy)
  }

}
