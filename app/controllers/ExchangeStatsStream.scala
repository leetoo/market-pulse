package controllers

import actors.Common.Exchange
import actors.ExchangeStatsAnnouncer
import actors.ExchangeStatsAnnouncer.ExchangeStats
import akka.actor.ActorRef
import play.api.libs.json.JsValue
import play.api.mvc.WebSocket.FrameFormatter
import play.api.mvc._
import play.api.libs.json._
import play.api.Play.current

trait ExchangeStatsStream {

  implicit val exchangeStatsFormat = Json.format[ExchangeStats]
  implicit val exchangeStatsFrameFormatter = FrameFormatter.jsonFrame[ExchangeStats]

  def socket = WebSocket.acceptWithActor[String, ExchangeStats] { implicit request => out => newAnnouncer(out) }

  def newAnnouncer(out: ActorRef) = ExchangeStatsAnnouncer.props(out)

}

object ExchangeStatsStream extends Controller with ExchangeStatsStream