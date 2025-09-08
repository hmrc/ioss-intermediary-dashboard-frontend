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

package forms

import play.api.data.Form
import play.api.data.Forms.*

object ConversationData {

  def apply(): Form[ConversationData] =
    Form[ConversationData](
      mapping(
        "query" -> tuple(
          "subject" -> optional(text).verifying("Subject is required", _.isDefined),
          "message" -> optional(text).verifying("Message is required", _.isDefined),
          "language" -> optional(text)
        ),
        "sender" -> tuple(
          "name" -> optional(text).verifying("Sender name is required", _.isDefined),
          "conversation-id" -> optional(text).verifying("Sender conversation Id is required", _.isDefined),
          "identifier-name" -> optional(text).verifying("Identifier name is required", _.isDefined),
          "identifier-value" -> optional(text).verifying("Identifier value is required", _.isDefined),
          "display-name" -> optional(text).verifying("Sender display name is required", _.isDefined)
        ),
        "sender-parameter1" -> tuple("key" -> optional(text), "value" -> optional(text)),
        "sender-parameter2" -> tuple("key" -> optional(text), "value" -> optional(text)),
        "customer" -> tuple(
          "name" -> optional(text),
          "email" -> optional(text),
          "enrolment-key" -> optional(text).verifying("Customer enrolment key is required", _.isDefined),
          "enrolment-name" -> optional(text).verifying("Customer Enrolment name is required", _.isDefined),
          "enrolment-value" -> optional(text).verifying("Customer Enrolment value is required", _.isDefined)
        ),
        "alert.template-id" -> optional(text).verifying("Alert template is mandatory", _.isDefined),
        "alert-parameter1" -> tuple("key" -> optional(text), "value" -> optional(text)),
        "alert-parameter2" -> tuple("key" -> optional(text), "value" -> optional(text)),
        "tags-parameter1" -> tuple("key" -> optional(text), "value" -> optional(text)),
        "tags-parameter2" -> tuple("key" -> optional(text), "value" -> optional(text)),
        "tags-parameter3" -> tuple("key" -> optional(text), "value" -> optional(text)),
        "tags-parameter4" -> tuple("key" -> optional(text), "value" -> optional(text)),
        "tags-parameter5" -> tuple("key" -> optional(text), "value" -> optional(text))
      )(ConversationData.apply)(c => Some(Tuple.fromProductTyped[ConversationData](c)))
    )

  case class ConversationData(
                               query: (Option[String], Option[String], Option[String]),
                               sender: (Option[String], Option[String], Option[String], Option[String], Option[String]),
                               senderParameter1: (Option[String], Option[String]),
                               senderParameter2: (Option[String], Option[String]),
                               customer: (Option[String], Option[String], Option[String], Option[String], Option[String]),
                               alertTemplate: Option[String],
                               alertParameter1: (Option[String], Option[String]),
                               alertParameter2: (Option[String], Option[String]),
                               tagsParameter1: (Option[String], Option[String]),
                               tagsParameter2: (Option[String], Option[String]),
                               tagsParameter3: (Option[String], Option[String]),
                               tagsParameter4: (Option[String], Option[String]),
                               tagsParameter5: (Option[String], Option[String])
                             )
 }
