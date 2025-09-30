/*
 * Copyright 2025 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
