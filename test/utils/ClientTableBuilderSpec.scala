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

package utils

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalactic.Prettifier.default
import play.api.i18n.{DefaultMessagesApi, Lang, Messages, MessagesImpl}

class ClientTableBuilderSpec extends AnyFreeSpec with Matchers {

  "ClientTableBuilder." - {

    ".sortClients" - {

      "must sort clients by activationExpiryDate, earliest first" in {
        val names = Seq("Beta Works", "Alpha Works", "Gamma Works")
        val dates = Seq("25 September 2025", "24 September 2025", "23 September 2025")
        val urls = Seq("/test-url/1", "/test-url/2", "/test-url/3")

        val result = ClientTableBuilder.sortClients(names, dates, urls)

        result.map(_._1) mustBe Seq("Gamma Works", "Alpha Works", "Beta Works")
      }

      "must sort alphabetically when activationExpiryDate are the same" in {
        val names = Seq("Beta Works", "Alpha Works", "Gamma Works")
        val dates = Seq("25 September 2025", "25 September 2025", "25 September 2025")
        val urls = Seq("/test-url/1", "/test-url/2", "/test-url/3")

        val result = ClientTableBuilder.sortClients(names, dates, urls)

        result.map(_._1) mustBe Seq("Alpha Works", "Beta Works", "Gamma Works")
      }

      "must return empty sequence if no clients provided" in {
        ClientTableBuilder.sortClients(Nil, Nil, Nil) mustBe Nil
      }
    }

    ".buildClientsTable" - {

      "must render rows with name, hidden text and expiry date" in {

        val messagesApi = new DefaultMessagesApi(Map("en" -> Map(
          "clientAwaitingActivation.link" -> "<a class=\"govuk-link\" href=\"{1}\"><span class=\"govuk-visually-hidden\">{2}</span>{0}</a>",
          "clientAwaitingActivation.hidden" -> "view registration details for"
        )))

        implicit val messages: Messages = MessagesImpl(Lang("en"), messagesApi)

        val names = Seq("Alpha Works")
        val dates = Seq("2025-09-24")
        val urls = Seq("/test-url/2")

        val table = ClientTableBuilder.buildClientsTable(names, dates, urls)

        val firstRow = table.rows.head

        val html = firstRow.head.content.asHtml.toString

        html must include("Alpha Works")
        html must include("view registration details for")
        html must include("/test-url/2")

        val expiry = firstRow(1).content.asHtml.toString
        expiry must include("2025-09-24")
      }
    }
  }
}
