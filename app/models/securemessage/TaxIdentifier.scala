/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package models.securemessage

import play.api.libs.json.{Json, OFormat}

case class TaxIdentifier(name: String, value: String)

object TaxIdentifier {
  implicit val formats: OFormat[TaxIdentifier] = Json.format[TaxIdentifier]
}
