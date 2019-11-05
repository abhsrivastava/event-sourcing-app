package dao

import model.Tag
import java.util.UUID
import scala.collection.mutable.{Map => MMap}
import com.appliedscala.events.LogRecord
import com.appliedscala.events.tag.{TagCreated, TagDeleted}

class InMemoryReadDao {
    val tags = MMap.empty[UUID, Tag]
    def init(records: List[LogRecord]) : Unit = {
        records.foreach(processEvent)
    }
    def processEvent(record: LogRecord) : Unit = {
        record.action match {
            case TagCreated.actionName => 
                val event = record.data.as[TagCreated]
                tags += (event.id -> Tag(event.id, event.text))
            case TagDeleted.actionName =>
                val event = record.data.as[TagDeleted]
                tags -= event.id
            case _ => ()
        }
    }
    def getTags() : List[Tag] = {
        tags.values.toList.sorted(Tag.tagOrdering)
    }
}