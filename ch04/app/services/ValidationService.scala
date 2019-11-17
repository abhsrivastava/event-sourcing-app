package services

import akka.actor.ActorSystem
import actors.ValidationActor
import dao.ValidationDao
import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import akka.pattern.ask
import com.appliedscala.events.LogRecord
import akka.util.Timeout

class ValidationService(actorSystem: ActorSystem, validationDao : ValidationDao) {
    import ValidationActor._
    val validationActor = actorSystem.actorOf(
        ValidationActor.props(validationDao), ValidationActor.name
    )
    implicit val timeout = Timeout(5 seconds)
    def validate(event: LogRecord) : Future[Option[String]] = {
        (validationActor ? ValidationRequest(event)).mapTo[Option[String]]
    }
}