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

class RecipientSpec extends AnyFreeSpec with Matchers {

  "Recipient" - {

    "must serialise and deserialise correctly from and to a Recipient" - {

      "with all optional fields present" in {

        val recipient: Recipient = Recipient(
          regime = "regime",
          taxIdentifier = TaxIdentifier(name = "name", value = "value"),
          name = Some(RecipientName(
            line1 = Some("line1"), line2 = Some("line2"), line3 = Some("line3")
          )),
          email = Some("email"))

        val expectedJson = Json.obj(
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
        )

        Json.toJson(recipient) mustBe expectedJson
        expectedJson.validate[Recipient] mustBe JsSuccess(recipient)
      }

      "with all optional fields missing" in {

        val recipient: Recipient = Recipient(
          regime = "regime",
          taxIdentifier = TaxIdentifier(name = "name", value = "value"),
          name = None,
          email = None)

        val expectedJson = Json.obj(
          "regime" -> "regime",
          "taxIdentifier" -> Json.obj(
            "name" -> "name",
            "value" -> "value"
          )
        )

        Json.toJson(recipient) mustBe expectedJson
        expectedJson.validate[Recipient] mustBe JsSuccess(recipient)
      }
    }
  }
}
