/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package models.securemessage

import play.api.libs.json.{Json, OFormat}

case class SecureMessageDetails(formId: String, issueDate: Option[String], batchId: Option[String], sourceDate: Option[String], properties: Option[Properties])

object SecureMessageDetails {
  implicit val formats: OFormat[SecureMessageDetails] = Json.format[SecureMessageDetails]
}


case class Properties(property: Property)

object Properties {
  implicit val formats: OFormat[Properties] = Json.format[Properties]
}


case class Property(name: String, value: String)

object Property {
  implicit val formats: OFormat[Property] = Json.format[Property]
}
