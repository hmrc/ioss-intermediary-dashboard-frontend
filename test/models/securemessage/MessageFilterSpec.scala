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

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.{JsSuccess, Json}

class MessageFilterSpec extends AnyFreeSpec with Matchers {

  "MessageFilter" - {

    "must serialise and deserialise correctly from and to a MessageFilter" in {

      val messageFilter: MessageFilter = MessageFilter(taxIdentifiers = Seq("taxIdentifiers"), regimes = Seq("regimes"))

      val expectedJson = Json.obj(
        "taxIdentifiers" -> Json.arr("taxIdentifiers"),
        "regimes" -> Json.arr("regimes")
      )

      Json.toJson(messageFilter) mustBe expectedJson
      expectedJson.validate[MessageFilter] mustBe JsSuccess(messageFilter)
    }
  }
}

