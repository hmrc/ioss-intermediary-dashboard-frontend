/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package models.securemessage

import play.api.libs.json.*

import java.time.LocalDate

case class SecureMessage(
                          externalReference: ExternalReference,
                          recipient: Recipient,
                          tags: Option[SecureMessageTags],
                          messageType: String,
                          content: Seq[Content],
                          language: Option[String],
                          validFrom: Option[LocalDate],
                          alertDetails: Option[SecureMessageAlertDetails],
                          alertQueue: Option[String],
                          details: Option[SecureMessageDetails]
                        )

object SecureMessage {

  val reads: Reads[SecureMessage] = {

    import play.api.libs.functional.syntax.*

    (
      (__ \ "externalReference").read[ExternalReference] and
      (__ \ "recipient").read[Recipient] and
      (__ \ "tags").readNullable[SecureMessageTags] and
      (__ \ "messageType").read[String] and
      (__ \ "content").read[Seq[Content]] and
      (__ \ "language").readNullable[String] and
      (__ \ "validFrom").readNullable[LocalDate] and
      (__ \ "alertDetails").readNullable[SecureMessageAlertDetails] and
      (__ \ "alertQueue").readNullable[String] and
      (__ \ "details").readNullable[SecureMessageDetails]
    )(SecureMessage.apply _)
  }

  val writes: OWrites[SecureMessage] = {

    import play.api.libs.functional.syntax.*

    (
      (__ \ "externalReference").write[ExternalReference] and
      (__ \ "recipient").write[Recipient] and
      (__ \ "tags").writeNullable[SecureMessageTags] and
      (__ \ "messageType").write[String] and
      (__ \ "content").write[Seq[Content]] and
      (__ \ "language").writeNullable[String] and
      (__ \ "validFrom").writeNullable[LocalDate] and
      (__ \ "alertDetails").writeNullable[SecureMessageAlertDetails] and
      (__ \ "alertQueue").writeNullable[String] and
      (__ \ "details").writeNullable[SecureMessageDetails]
      )(secureMessage => Tuple.fromProductTyped(secureMessage))
  }

  implicit val format: OFormat[SecureMessage] = OFormat(reads, writes)

}
