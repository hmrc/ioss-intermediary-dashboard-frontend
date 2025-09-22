/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package models.securemessage

import play.api.libs.json.{Json, OFormat}

case class Content(lang: String, subject: String, body: String)

object Content {
  implicit val formats: OFormat[Content] = Json.format[Content]
}
