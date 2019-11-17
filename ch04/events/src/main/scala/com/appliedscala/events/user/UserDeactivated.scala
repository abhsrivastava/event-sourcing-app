package com.appliedscala.events.user

import java.util.UUID
import com.appliedscala.events.EventData
import play.api.libs.json._

case class UserDeactivated(id: UUID) extends EventData {
    override def action: String = UserDeactivated.actionName
    override def json = Json.writes[UserDeactivated].writes(this)
}
object UserDeactivated {
    val actionName = "user-deactivated"
    implicit val reads = Json.reads[UserDeactivated]
}