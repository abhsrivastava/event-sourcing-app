package services

import akka.actor._
import play.api.Logger
import akka.util.Timeout
import scala.concurrent.duration._
import actors._
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import model._
import dao._
import akka.pattern.ask
import scala.util.{Success, Failure}

class ReadService(actorSystem: ActorSystem, logDao: LogDao) {
    val log = Logger(this.getClass)
    implicit val timeout = Timeout(5 seconds)
    
    def init() : Unit = {
        logDao.getLogRecords() match {
            case Failure(th) => 
                log.error("Error in initializing the read servcie", th)
                throw th
            case Success(logRecords) => 
                val actor = actorSystem.actorOf(InMemoryReadActor.props(logRecords), InMemoryReadActor.name)
                actor ! InMemoryReadActor.InitializeState
        }
    }
    def getTags : Future[List[Tag]] = {
        val actor = actorSystem.actorSelection(InMemoryReadActor.path)
        (actor ? InMemoryReadActor.GetTags).mapTo[List[Tag]]
    }
}