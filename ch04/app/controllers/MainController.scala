package controllers

import controllers.Assets.Asset
import model._
import play.api.mvc._
import security.{UserAuthAction, UserAwareAction, UserAwareRequest}
import akka.actor.{ActorSystem}
import akka.stream.Materializer
import actors.EventStreamActor
import play.api.libs.EventSource
import akka.stream.scaladsl._
import play.api.http.ContentTypes
import play.api.libs.json.JsValue
import akka.stream.OverflowStrategy
import services.{LogRecordConsumer, TagEventConsumer}

class MainController(
  components: ControllerComponents, 
  assets: Assets, 
  userAuthAction: UserAuthAction,
  userAwareAction: UserAwareAction,
  actorSystem: ActorSystem,
  logRecordConsumer: LogRecordConsumer,
  tagEventConsumer: TagEventConsumer,
  mat: Materializer) extends AbstractController(components) {

  def index = userAwareAction { request =>
    Ok(views.html.pages.react(buildNavData(request),
      WebPageData("Home")))
  }

  def error500 = Action {
    InternalServerError(views.html.errorPage())
  }

  private def buildNavData(request: UserAwareRequest[_]): NavigationData = {
    NavigationData(request.user, isLoggedIn = request.user.isDefined)
  }

  def versioned(path: String, file: Asset) = assets.versioned(path, file)

  def serverEventStream = userAwareAction { request => 
    implicit val materializer = mat
    implicit val actorFactory = actorSystem
    val mayBeUser = request.user
    val mayBeUserId = request.user.map(_.userId)
    val (out, publisher) = Source
                            .actorRef[JsValue](bufferSize = 16, OverflowStrategy.dropNew)
                            .toMat(Sink.asPublisher(fanout = false))(Keep.both).run()
    val actor = actorSystem.actorOf(EventStreamActor.props(out), EventStreamActor.name(mayBeUserId))
    val source = Source.fromPublisher(publisher)
    Ok.chunked(source.via(EventSource.flow)).as(ContentTypes.EVENT_STREAM)
  }
}
