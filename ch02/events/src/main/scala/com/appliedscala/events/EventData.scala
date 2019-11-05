package com.appliedscala.events

import play.api.libs.json._
import java.util.UUID

trait EventData {
    def action: String
    def json: JsValue
}