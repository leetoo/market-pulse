package actors

import actors.Common._
import actors.ExchangeAccountant.{SaleAccepted, Sale}
import actors.Receptionist.{Trade, TradeAccepted}
import akka.actor.Status.Failure
import akka.actor._

import scala.concurrent.duration._

object Receptionist {

  case class Trade(userId: UserId,
                   currencyFrom: Currency,
                   currencyTo: Currency,
                   amountSell: Amount,
                   amountBuy: Amount,
                   rate: Rate,
                   timePlaced: DateTime,
                   originatingCountry: Country) {

    lazy val exchange: Exchange = (currencyFrom, currencyTo)

  }

  object TradeAccepted

}

class Receptionist extends Actor with ActorLogging {

  private case class Transaction(client: ActorRef, accountant: ActorRef, sale: Sale)

  private val exchangeAccountants = scala.collection.mutable.Map[Exchange, ActorRef]()
  private val idSequences = scala.collection.mutable.Map[Exchange, Int]()

  private var transactionsInProgress = Set[Transaction]()

  context.setReceiveTimeout(100 milliseconds)

  override def receive: Receive = {
    case trade: Trade => {
      val accountant = exchangeAccountants.getOrElseUpdate(trade.exchange, newAccountant(trade.exchange))
      val sale = Sale(idSequences.getOrElseUpdate(trade.exchange, 0), trade.amountSell, trade.amountBuy)

      transactionsInProgress = transactionsInProgress + Transaction(sender(), accountant, sale)

      accountant ! sale

      idSequences.update(trade.exchange, sale.id + 1)
    }
    case SaleAccepted(id) => {
      transactionsInProgress.find(_.sale.id == id) match {
        case Some(tx) => {
          tx.client ! TradeAccepted
          transactionsInProgress = transactionsInProgress - tx
        }
        case None => log.warning("Transaction not found ({}). Unable to confirm.", id)
      }
    }
    case ReceiveTimeout => transactionsInProgress.foreach(tx => tx.accountant ! tx.sale)
    case msg => {
      log.error(s"Failed to process message: ${msg}")
      sender ! Failure(new IllegalArgumentException("Trade processing failed"))
    }
  }

  def newAccountant(exchange: Exchange) = context.actorOf(Props(classOf[ExchangeAccountant], exchange))

}


