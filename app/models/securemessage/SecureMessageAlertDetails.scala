/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package models.securemessage

import play.api.libs.json.{Json, OFormat}

case class SecureMessageAlertDetails(data: AlertDetailsData)

object SecureMessageAlertDetails {
  implicit val formats: OFormat[SecureMessageAlertDetails] = Json.format[SecureMessageAlertDetails]
}


case class AlertDetailsData(key1: String, key2: String)

object AlertDetailsData {
  implicit val formats: OFormat[AlertDetailsData] = Json.format[AlertDetailsData]
}
