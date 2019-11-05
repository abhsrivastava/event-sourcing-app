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
import play.api.Configuration
import util.ServiceKafkaProducer

class TagEventProducer(actorSystem: ActorSystem, configuration: Configuration) {
    val kafkaServiceProducer = new ServiceKafkaProducer("tags", actorSystem, configuration)

    private def createLogRecord(eventData: EventData) : LogRecord = {
        LogRecord(UUID.randomUUID(), eventData.action, eventData.json, ZonedDateTime.now())
    }
    def createTag(text: String, createdBy: UUID) : Unit = {
        val event = TagCreated(UUID.randomUUID(), text, createdBy)
        val record = createLogRecord(event)
        kafkaServiceProducer.send(record.encode)
    }
    def deleteTag(tag: UUID, deletedBy: UUID) : Unit = {
        val event = TagDeleted(tag, deletedBy)
        val record = createLogRecord(event)
        kafkaServiceProducer.send(record.encode)
    }
}