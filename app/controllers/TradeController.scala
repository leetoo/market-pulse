package controllers

import actors.Receptionist
import actors.Receptionist.{TradeAccepted, Trade}
import akka.actor.{ActorRef, Props}
import akka.pattern.ask
import akka.util.Timeout
import play.api.Play.current
import play.api.libs.concurrent.Akka
import play.api.libs.json._
import play.api.mvc.{Action, Controller}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

trait TradeController {
  this: Controller =>

  implicit val timeout = Timeout(2 seconds)
  implicit val tradeReads = Json.reads[Trade]

  val receptionist = Akka.system.actorOf(Props[Receptionist], name = "receptionist")

  val create = Action.async(parse.json) { implicit request =>
    val tradeResult = request.body.validate[Trade]
    tradeResult.fold(
      errors => {
        Future(BadRequest(Json.obj("message" -> "Validation error",
                                   "details" -> JsError.toFlatJson(errors))))
      },
      trade => {
        (receptionist ? trade).mapTo[TradeAccepted.type].map { msg => Ok(Json.obj("message" -> s"Trade accepted")) }
      }
    )
  }

}

object TradeController extends Controller with TradeController
