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

package utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Table
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.{HeadCell, TableRow}

object ClientTableBuilder {

  private val dateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH)

  private def parseDate(dateStr: String): LocalDate = LocalDate.parse(dateStr, dateFormatter)
  
  def sortClients(
                  clientCompanyNames: Seq[String],
                  activationExpiryDates: Seq[String],
                  pendingRegistrationUrls: Seq[String]
                 ): Seq[(String, String, String)] = {

    val combined: Seq[(String, String, String)] =
      clientCompanyNames
        .lazyZip(activationExpiryDates)
        .lazyZip(pendingRegistrationUrls)
        .map((name, expiryDate, url) => (name, expiryDate, url))
      
    combined.sortBy { case (name, expiryDate, _) => (parseDate(expiryDate), name)}
  }

  def buildClientsTable(
                         clientCompanyNames: Seq[String],
                         activationExpiryDates: Seq[String],
                         pendingRegistrationUrls: Seq[String]
                         )(implicit messages: Messages): Table = {

    val rows: Seq[Seq[TableRow]] =
      sortClients(clientCompanyNames, activationExpiryDates, pendingRegistrationUrls)
        .map { case (name, expiryDate, url) =>
          
          Seq(
            TableRow(
              content = HtmlContent(
                messages(
                  "clientAwaitingActivation.link",
                  name,
                  url,
                  messages("clientAwaitingActivation.hidden", name)
                )
              )
            ),
            TableRow(
              content = Text(expiryDate)
            )
          )
        }

    Table(
      rows,
      head = Some(Seq(
        HeadCell(content = Text("Client name")),
        HeadCell(content = Text("Expiry date"))
      ))
    )
    
  }
}
