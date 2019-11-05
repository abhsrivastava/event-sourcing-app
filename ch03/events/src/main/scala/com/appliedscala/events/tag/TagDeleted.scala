package com.appliedscala.events.tag

import com.appliedscala.events._
import play.api.libs.json._
import java.util.UUID

case class TagDeleted(id: UUID, deletedBy: UUID) extends EventData {
    override def action = TagDeleted.actionName
    override def json = Json.writes[TagDeleted].writes(this)
}

object TagDeleted {
    val actionName = "tag-deleted"
    implicit val reads : Reads[TagDeleted] = Json.reads[TagDeleted]
}