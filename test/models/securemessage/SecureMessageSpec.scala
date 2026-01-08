/*
 * Copyright 2026 HM Revenue & Customs
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

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.{JsSuccess, Json}

import java.time.LocalDate

class SecureMessageSpec extends AnyFreeSpec with Matchers {

  "SecureMessage" - {

    "must serialise and deserialise correctly from and to a SecureMessage" - {

      "with all optional fields present" in {

        val secureMessage: SecureMessage = SecureMessage(
          externalReference = ExternalReference(id = "id", source = "source"),
          recipient = Recipient(
            regime = "regime",
            taxIdentifier = TaxIdentifier(name = "name", value = "value"),
            name = Some(RecipientName(
              line1 = Some("line1"), line2 = Some("line2"), line3 = Some("line3")
            )),
            email = Some("email")),
          tags = Some(SecureMessageTags(notificationType = Some("notificationType"))),
          messageType = "messageType",
          content = Seq(Content(lang = "lang", subject = "subject", body = "body")),
          language = Some("language"),
          validFrom = Some(LocalDate.now),
          alertDetails = Some(SecureMessageAlertDetails(
            data = AlertDetailsData(
              key1 = "key1", key2 = "key2"
            )
          )),
          alertQueue = Some("alertQueue"),
          details = Some(SecureMessageDetails(
            formId = "formId",
            issueDate = Some("issueDate"),
            batchId = Some("batchId"),
            sourceDate = Some("sourceDate"),
            properties = Some(Properties(
              property = Property(name = "name", value = "value")
            ))
          ))
        )

        val expectedJson = Json.obj(
          "externalReference" -> Json.obj(
            "id" -> "id",
            "source" -> "source"
          ),
          "recipient" -> Json.obj(
            "regime" -> "regime",
            "taxIdentifier" -> Json.obj(
              "name" -> "name",
              "value" -> "value"
            ),
            "name" -> Json.obj(
              "line1" -> "line1",
              "line2" -> "line2",
              "line3" -> "line3"
            ),
            "email" -> "email"
          ),
          "tags" -> Json.obj(
            "notificationType" -> "notificationType"
          ),
          "messageType" -> "messageType",
          "content" -> Json.arr(
            Json.obj(
              "lang" -> "lang",
              "subject" -> "subject",
              "body" -> "body"
            )
          ),
          "language" -> "language",
          "validFrom" -> LocalDate.now.toString,
          "alertDetails" -> Json.obj(
            "data" -> Json.obj(
              "key1" -> "key1",
              "key2" -> "key2"
            )
          ),
          "alertQueue" -> "alertQueue",
          "details" -> Json.obj(
            "formId" -> "formId",
            "issueDate" -> "issueDate",
            "batchId" -> "batchId",
            "sourceDate" -> "sourceDate",
            "properties" -> Json.obj(
              "property" -> Json.obj(
                "name" -> "name",
                "value" -> "value"
              )
            )
          )
        )

        Json.toJson(secureMessage) mustBe expectedJson
        expectedJson.validate[SecureMessage] mustBe JsSuccess(secureMessage)
      }

      "with all optional fields missing" in {

        val secureMessage: SecureMessage = SecureMessage(
          externalReference = ExternalReference(id = "id", source = "source"),
          recipient = Recipient(
            regime = "regime",
            taxIdentifier = TaxIdentifier(name = "name", value = "value"),
            name = None,
            email = None),
          tags = None,
          messageType = "messageType",
          content = Seq(Content(lang = "lang", subject = "subject", body = "body")),
          language = None,
          validFrom = None,
          alertDetails = None,
          alertQueue = None,
          details = None
        )

        val expectedJson = Json.obj(
          "externalReference" -> Json.obj(
            "id" -> "id",
            "source" -> "source"
          ),
          "recipient" -> Json.obj(
            "regime" -> "regime",
            "taxIdentifier" -> Json.obj(
              "name" -> "name",
              "value" -> "value"
            )
          ),
          "messageType" -> "messageType",
          "content" -> Json.arr(
            Json.obj(
              "lang" -> "lang",
              "subject" -> "subject",
              "body" -> "body"
            )
          )
        )

        Json.toJson(secureMessage) mustBe expectedJson
        expectedJson.validate[SecureMessage] mustBe JsSuccess(secureMessage)
      }
    }
  }
}
