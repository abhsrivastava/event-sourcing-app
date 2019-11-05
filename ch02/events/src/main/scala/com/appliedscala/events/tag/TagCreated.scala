package com.appliedscala.events.tag

import com.appliedscala.events._
import play.api.libs.json._
import java.util.UUID

case class TagCreated(id: UUID, text: String, createdBy: UUID) extends EventData {
    override def action = TagCreated.actionName
    override def json = Json.writes[TagCreated].writes(this)
}

object TagCreated {
    val actionName = "tag-created"
    implicit val reads : Reads[TagCreated] = Json.reads[TagCreated]
}