package actors

import akka.actor.{Actor, Props}
import dao.ValidationDao
import com.appliedscala.events.LogRecord
import com.appliedscala.events.user.{UserActivated, UserDeactivated}
import com.appliedscala.events.tag.{TagCreated, TagDeleted}
import com.appliedscala.events.question.{QuestionCreated, QuestionDeleted}

class ValidationActor(dao: ValidationDao) extends Actor {
    import ValidationActor._
    import dao._
    override def receive : Receive = {
        case ValidationRequest(event) => processSingleEvent(event, skipValidation = false)
        case RefreshStateCommand(events, fromScratch) => 
            val result = resetState(fromScratch) match {
                case Some(x) => Some(x)
                case None => processEvents(events, skipValidation = true)
            }
            sender() ! result
        case _ => sender() ! Some("Unknown message type")
    }
    def processEvents(events: List[LogRecord], skipValidation: Boolean) : Option[String] = {
        val list = events.map(event => processSingleEvent(event, skipValidation)).takeWhile(_.isDefined)
        list.reverse.headOption.flatten
    }
    def processSingleEvent(event: LogRecord, skipValidation: Boolean) : Option[String] = {
        event.action match {
            case UserActivated.actionName => 
                val user = event.data.as[UserActivated]
                validateAndUpdate(skipValidation){validateUserActivated(user.id)}{updateUserActivated(user.id)}
            case UserDeactivated.actionName => 
                val user = event.data.as[UserDeactivated]
                validateAndUpdate(skipValidation){validateUserDeactivated(user.id)}{updateUserDeactivated(user.id)}
            case TagCreated.actionName =>
                val tag = event.data.as[TagCreated]
                validateAndUpdate(skipValidation){validateTagCreated(tag.text, tag.createdBy)}{updateTagCreated(tag.id, tag.text)}
            case TagDeleted.actionName => 
                val tag = event.data.as[TagDeleted]
                validateAndUpdate(skipValidation){validateTagDeleted(tag.id, tag.deletedBy)}{updateTagDeleted(tag.id)}
            case QuestionCreated.actionName =>
                val q = event.data.as[QuestionCreated]
                validateAndUpdate(skipValidation){validateQuestionCreated(q.questionId, q.createdBy, q.tags)}{updateQuestionCreated(q.questionId, q.createdBy, q.tags)}
            case QuestionDeleted.actionName =>
                val q = event.data.as[QuestionDeleted]
                validateAndUpdate(skipValidation){validateQuestionDeleted(q.questionId, q.deletedBy)}{updateQuestionDeleted(q.questionId)}
            case _ =>
                Some("Unknown event")
        }
    }
}

object ValidationActor {
    case class ValidationRequest(event: LogRecord)
    case class RefreshStateCommand(events: List[LogRecord], fromScratch: Boolean = true)
    val name = "validation-actor"
    val path = s"/user/$name"
    def props(dao: ValidationDao) = Props(new ValidationActor(dao))
}