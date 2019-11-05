package services

import com.appliedscala.events.{EventData, LogRecord}
import akka.actor.ActorSystem
import dao.LogDao
import java.util.UUID
import java.time.ZonedDateTime
import scala.concurrent.duration._
import akka.util.Timeout
import scala.concurrent.Future
import model.Tag
import actors.InMemoryReadActor
import akka.pattern.ask
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Success, Failure}
import com.appliedscala.events.tag.{TagCreated, TagDeleted}

class TagEventProducer(actorSystem: ActorSystem, logDao : LogDao, readService: ReadService) {
    private def createLogRecord(eventData: EventData) : LogRecord = {
        LogRecord(UUID.randomUUID(), eventData.action, eventData.json, ZonedDateTime.now())
    }
    private def adjustReadState(logRecord: LogRecord) : Future[List[Tag]] = {
        implicit val timeout = Timeout(5 seconds)
        val actor = actorSystem.actorSelection(InMemoryReadActor.path)
        (actor ? InMemoryReadActor.ProcessEvent(logRecord)).flatMap { _ => 
            readService.getTags
        }
    }
    private def processTag(event: EventData) : Future[List[Tag]] = {
        val record = createLogRecord(event)
        logDao.insertLogRecord(record) match {
            case Success(_) => adjustReadState(record)
            case Failure(th) => Future.failed(th)
        }
    }
    def createTag(text: String, createdBy: UUID) : Future[List[Tag]] = {
        val event = TagCreated(UUID.randomUUID(), text, createdBy)
        processTag(event)
    }
    def deleteTag(tag: UUID, deletedBy: UUID) : Future[List[Tag]] = {
        val event = TagDeleted(tag, deletedBy)
        processTag(event)
    }
}