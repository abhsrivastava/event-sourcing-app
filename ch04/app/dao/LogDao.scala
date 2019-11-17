package dao

import com.appliedscala.events.LogRecord._
import com.appliedscala.events._
import scalikejdbc._
import scala.util.Try

class LogDao {
    def insertLogRecord(event: LogRecord) : Try[Unit] = Try {
        NamedDB('eventstore).localTx{implicit session => 
            val jsonStr = event.data.toString()
            sql"""
                insert into logs(record_id, action_name, event_data, timestamp) values(
                    ${event.id}, ${event.action}, $jsonStr, ${event.timestamp}
                )
            """.update().apply()
        }
    }
    def getLogRecords() : Try[List[LogRecord]] = Try {
        NamedDB('eventstore).readOnly{implicit session => 
            sql"""
                select record_id, action_name, event_data, timestamp from logs
            """.map(rs2LogRecord).list().apply()
        }
    }
}