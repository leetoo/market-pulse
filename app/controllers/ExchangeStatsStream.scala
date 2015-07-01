package controllers

import actors.ExchangeStatsAnnouncer
import actors.ExchangeStatsAnnouncer.{AnnouncementRequest, ExchangeStats}
import akka.actor.ActorRef
import play.api.Play.current
import play.api.libs.json._
import play.api.mvc.WebSocket.FrameFormatter
import play.api.mvc._

trait ExchangeStatsStream {

  implicit val announcementRequestFormat = Json.format[AnnouncementRequest]
  implicit val announcementRequestFormatter = FrameFormatter.jsonFrame[AnnouncementRequest]

  implicit val exchangeStatsFormat = Json.format[ExchangeStats]
  implicit val exchangeStatsFrameFormatter = FrameFormatter.jsonFrame[ExchangeStats]

  def socket = WebSocket.acceptWithActor[AnnouncementRequest, ExchangeStats] { implicit request => out => newAnnouncer(out) }

  def newAnnouncer(out: ActorRef) = ExchangeStatsAnnouncer.props(out)

}

object ExchangeStatsStream extends Controller with ExchangeStatsStream