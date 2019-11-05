package services
import akka.actor.ActorSystem
import dao.LogDao
import play.api.Configuration
import akka.stream.Materializer
import util.ServiceKafkaConsumer
import com.appliedscala.events.LogRecord

class LogRecordConsumer(
    actorSystem: ActorSystem, 
    logDao: LogDao, 
    configuration: Configuration,
    materializer: Materializer
) {
    val topics = Set("tags")
    val serviceKafkaConsumer = new ServiceKafkaConsumer(
        topics,
        "log",
        actorSystem,
        configuration,
        handleEvent,
        materializer
    )
    def handleEvent(event: String) : Unit = {
        val mayBeGenericEnvelop = LogRecord.decode(event)
        mayBeGenericEnvelop.foreach{logRecord => 
            logDao.insertLogRecord(logRecord)
        }
    }
}