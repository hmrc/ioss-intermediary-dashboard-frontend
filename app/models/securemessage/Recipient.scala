/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package models.securemessage

import play.api.libs.json.{Json, OFormat}

case class Recipient(regime: String, taxIdentifier: TaxIdentifier, name: Option[RecipientName], email: Option[String])

object Recipient {
  implicit val formats: OFormat[Recipient] = Json.format[Recipient]
}
