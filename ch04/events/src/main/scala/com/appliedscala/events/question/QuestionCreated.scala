package com.appliedscala.events.question

import com.appliedscala.events.EventData
import play.api.libs.json._
import java.util.UUID
import java.time.ZonedDateTime

case class QuestionCreated(
    title: String, 
    details: Option[String], 
    tags: List[UUID], 
    questionId: UUID, 
    createdBy: UUID,
    createdAt: ZonedDateTime) extends EventData {
    import QuestionCreated._
    override def action = actionName
    override def json = Json.writes[QuestionCreated].writes(this)
}

object QuestionCreated {
    val actionName = "question-created"
    implicit val reads = Json.reads[QuestionCreated]
}