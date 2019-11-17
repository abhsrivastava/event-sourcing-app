package services

import akka.actor.ActorSystem
import akka.stream.Materializer
import play.api.Configuration
import util.ServiceKafkaConsumer
import com.appliedscala.events.LogRecord
import akka.util.Timeout
import scala.concurrent.duration._
import actors.EventStreamActor
import actors.InMemoryReadActor
import scala.concurrent.ExecutionContext.Implicits.global
import akka.pattern.ask
import model.ServerSentMessage

class TagEventConsumer(
    readService: ReadService, 
    actorSystem: ActorSystem, 
    configuration: Configuration, 
    materializer: Materializer) {
    
    val topicName = "tags"
    val serviceKafkaConsumer = new ServiceKafkaConsumer(
        topicNames = Set(topicName), 
        groupName = "read", 
        actorSystem = actorSystem, 
        configuration = configuration, 
        handleEvent = handleEvent, 
        mat = materializer)
    def handleEvent(event: String) : Unit = {
        val mayBeLogRecord = LogRecord.decode(event)
        mayBeLogRecord.foreach(adjustReadState)
    }
    private def adjustReadState(logRecord: LogRecord) : Unit = {
        implicit val timeout = Timeout(5 seconds)
        import InMemoryReadActor._
        val imrActor = actorSystem.actorSelection(InMemoryReadActor.path)
        (imrActor ? ProcessEvent(logRecord)).foreach{_ => 
            readService.getTags.foreach{tags => 
                val msg = ServerSentMessage.create("tags", tags)
                val esActor = actorSystem.actorSelection(EventStreamActor.pathPattern)
                esActor ! EventStreamActor.DataUpdated(msg.json)
            }
        }

    }
}