package com.appliedscala.events.question

import com.appliedscala.events.EventData
import play.api.libs.json._
import java.util.UUID
import java.time.ZonedDateTime

case class QuestionDeleted(
    title: String, 
    details: Option[String], 
    tags: List[UUID], 
    questionId: UUID, 
    deletedBy: UUID,
    deletedAt: ZonedDateTime) extends EventData {
    import QuestionDeleted._
    override def action = actionName
    override def json = Json.writes[QuestionDeleted].writes(this)
}

object QuestionDeleted {
    val actionName = "question-deleted"
    implicit val reads = Json.reads[QuestionDeleted]
}