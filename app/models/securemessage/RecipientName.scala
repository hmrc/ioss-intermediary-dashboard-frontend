/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package models.securemessage

import play.api.libs.json.{Json, OFormat}

case class RecipientName(line1: Option[String], line2: Option[String], line3: Option[String])

object RecipientName {
  implicit val formats: OFormat[RecipientName] = Json.format[RecipientName]
}
