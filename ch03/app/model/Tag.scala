package model

import java.util.UUID
import play.api.libs.json.Json

case class Tag(id: UUID, text: String)

object Tag{
    implicit def tagOrdering[A <: Tag] : Ordering[A] = Ordering.by(tag => tag.text)
    implicit val writes = Json.writes[Tag]
}