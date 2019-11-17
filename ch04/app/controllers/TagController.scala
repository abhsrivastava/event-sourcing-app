package controllers

import play.api.mvc.Controller
import services._
import play.api.mvc.ControllerComponents
import play.api.mvc.AbstractController
import security.UserAuthAction

import play.api.data.Form
import play.api.data.Forms._
import java.util.UUID

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.libs.json.Json

case class CreateTagData(text: String)
case class DeleteTagData(id: UUID)

class TagController(
    components: ControllerComponents, 
    tagEventProducer: TagEventProducer, 
    readService: ReadService,
    userAuthAction: UserAuthAction) extends AbstractController(components) {

        val createTagForm = Form {
            mapping(
                "text" -> nonEmptyText       
            )(CreateTagData.apply)(CreateTagData.unapply)
        }

        val deleteTagForm = Form {
            mapping(
                "id" -> uuid
            )(DeleteTagData.apply)(DeleteTagData.unapply)
        }

        def createTag() = userAuthAction.async{ implicit request => 
            createTagForm.bindFromRequest.fold(
                formWithErrors => Future.successful(BadRequest),
                data => {
                    tagEventProducer.createTag(data.text, request.user.userId)
                    Future.successful(Ok)
                }
            )
        }
        def deleteTag() = userAuthAction.async{ implicit request => 
            deleteTagForm.bindFromRequest.fold(
                formWithErrors => Future.successful(BadRequest),
                data => {
                    tagEventProducer.deleteTag(data.id, request.user.userId)
                    Future.successful(Ok)
                }
            )
        }
        def getTags = Action.async{implicit request => 
            readService.getTags.map{tags => Ok(Json.toJson(tags))}
        }
}