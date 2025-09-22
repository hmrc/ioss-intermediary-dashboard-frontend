/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package models.securemessage

import play.api.libs.json.{Json, OFormat}

case class ExternalReference(id: String, source: String)

object ExternalReference {
  implicit val formats: OFormat[ExternalReference] = Json.format[ExternalReference]
}
