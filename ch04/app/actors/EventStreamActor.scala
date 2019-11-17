package actors

import akka.actor.{Actor, ActorRef, Props}
import play.api.libs.json.{JsValue, JsObject, JsString}
import java.util.UUID
import netscape.javascript.JSObject

object EventStreamActor {
    def props(out: ActorRef)  = Props(new EventStreamActor(out))
    case class DataUpdated(jsValue: JsValue)
    case class ErrorOccurred(message: String)
    val name = "event-stream-actor"
    val pathPattern = s"/user/$name-*"
    def name(mayBeUserId: Option[UUID]) : String = {
        val randomPart = UUID.randomUUID().toString().split("-")(0)
        val userPart = mayBeUserId.fold("unregistred")(_.toString)
        s"$name-$userPart-$randomPart"
    }
    def userSpecificPathPattern(userId: UUID) = s"/user/$name-${userId.toString}-*"
}

class EventStreamActor(out: ActorRef) extends Actor {
    import EventStreamActor._
    override def receive = {
        case DataUpdated(js) => out ! js
        case ErrorOccurred(msg) => out ! JsObject(List("error" -> JsString(msg)))
    }
}
