package actors

import dao.InMemoryReadDao
import com.appliedscala.events.LogRecord
import akka.actor._

object InMemoryReadActor {
    case class ProcessEvent(event: LogRecord)
    case object InitializeState
    case object GetTags
    val name = "in-memory-read-actor"
    val path = s"/user/$name"
    def props(logRecords: List[LogRecord]) = Props(new InMemoryReadActor(logRecords))
}

class InMemoryReadActor(logRecords: List[LogRecord]) extends Actor {
    import InMemoryReadActor._
    val readDao = new InMemoryReadDao()
    override def receive: Receive = {
        case InitializeState => readDao.init(logRecords)
        case GetTags => sender() ! readDao.getTags()
        case ProcessEvent(event) => sender() ! readDao.processEvent(event)
    }
}