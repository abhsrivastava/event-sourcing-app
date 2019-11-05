package com.appliedscala.events

import java.util.UUID
import java.time.ZonedDateTime
import play.api.libs.json._
import scalikejdbc._

case class LogRecord(id: UUID, action: String, data: JsValue, timestamp: ZonedDateTime)
object LogRecord {
    def rs2LogRecord(rs: WrappedResultSet) : LogRecord = {
        LogRecord(
            UUID.fromString(rs.string("record_id")), 
            rs.string("action_name"), 
            Json.parse(rs.string("event_data")), 
            rs.dateTime("timestamp")
        )
    }
}