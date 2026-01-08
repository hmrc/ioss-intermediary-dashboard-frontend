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

class SecureMessageDetailsSpec extends AnyFreeSpec with Matchers {

  "SecureMessageDetails" - {

    "must serialise and deserialise correctly from and to a SecureMessageDetails" - {

      "with all optional fields present" in {

        val secureMessageDetails: SecureMessageDetails = SecureMessageDetails(
          formId = "formId",
          issueDate = Some("issueDate"),
          batchId = Some("batchId"),
          sourceDate = Some("sourceDate"),
          properties = Some(Properties(
            property = Property(name = "name", value = "value")
          ))
        )

        val expectedJson = Json.obj(
          "formId" -> "formId",
          "issueDate" -> "issueDate",
          "batchId" -> "batchId",
          "sourceDate" -> "sourceDate",
          "sourceDate" -> "sourceDate",
          "properties" -> Json.obj(
            "property" -> Json.obj(
              "name" -> "name",
              "value" -> "value"
            )
          )
        )

        Json.toJson(secureMessageDetails) mustBe expectedJson
        expectedJson.validate[SecureMessageDetails] mustBe JsSuccess(secureMessageDetails)
      }

      "with all optional fields missing" in {

        val secureMessageDetails: SecureMessageDetails = SecureMessageDetails(
          formId = "formId",
          issueDate = None,
          batchId = None,
          sourceDate = None,
          properties = None
        )

        val expectedJson = Json.obj(
          "formId" -> "formId"
        )

        Json.toJson(secureMessageDetails) mustBe expectedJson
        expectedJson.validate[SecureMessageDetails] mustBe JsSuccess(secureMessageDetails)
      }
    }
  }
}
