/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package models.securemessage

import play.api.libs.json.{Json, OFormat}

case class SecureMessageTags(notificationType: Option[String])

object SecureMessageTags {
  implicit val formats: OFormat[SecureMessageTags] = Json.format[SecureMessageTags]
}
