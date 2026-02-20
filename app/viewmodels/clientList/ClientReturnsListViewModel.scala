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

package viewmodels.clientList

import models.etmp.EtmpClientDetails
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{Table, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.{HeadCell, TableRow}

case class ClientReturnsListViewModel(
                                       activeClients: Table,
                                       excludedClients: Table
                                     )

object ClientReturnsListViewModel {

  def apply(
             clientReturnsList: Seq[EtmpClientDetails],
             redirectUrl: String
           )(implicit messages: Messages): ClientReturnsListViewModel = {

    val activeClients: Seq[EtmpClientDetails] = clientReturnsList.filterNot(_.clientExcluded)
    val excludedClients: Seq[EtmpClientDetails] = clientReturnsList.filter(_.clientExcluded)


    ClientReturnsListViewModel(
      activeClients = activeClientReturnsTable(activeClients, redirectUrl),
      excludedClients = excludedClientReturnsTable(excludedClients, redirectUrl)
    )
  }

  private def activeClientReturnsRows(
                                       activeClient: EtmpClientDetails,
                                       redirectUrl: String
                                     )(implicit messages: Messages): Seq[TableRow] = {
    Seq(
      TableRow(
        content = HtmlContent(
          messages(
            "clientReturnsList.change.link",
            activeClient.clientName,
            s"$redirectUrl/${activeClient.clientIossID}",
            messages("clientReturnsList.change.hidden", activeClient.clientName)
          )
        ),
        classes = "govuk-!-font-weight-regular"
      ),
      TableRow(
        content = Text(activeClient.clientIossID)
      )
    )
  }

  private def excludedClientReturnsRows(
                                         excludedClient: EtmpClientDetails,
                                         redirectUrl: String
                                       )(implicit messages: Messages): Seq[TableRow] = {
    Seq(
      TableRow(
        content = HtmlContent(
          messages(
            "clientReturnsList.change.link",
            excludedClient.clientName,
            s"$redirectUrl/${excludedClient.clientIossID}",
            messages("clientReturnsList.change.hidden", excludedClient.clientName)
          )
        ),
        classes = "govuk-!-font-weight-regular"
      ),
      TableRow(
        content = Text(excludedClient.clientIossID)
      ),
      TableRow(
        classes = "govuk-summary-list__row--no-actions"
      )
    )
  }

  private def activeClientReturnsTable(
                                        activeClients: Seq[EtmpClientDetails],
                                        redirectUrl: String
                                      )(implicit messages: Messages): Table = {

    val activeClientsRows = activeClients.map { activeClient =>
      activeClientReturnsRows(activeClient, redirectUrl)
    }

    Table(
      rows = activeClientsRows,
      head = Some(Seq(
        HeadCell(
          content = Text(messages("clientReturnsList.table.header.clientName")),
          classes = "govuk-!-width-three-quarters"
        ),
        HeadCell(
          content = Text(messages("clientReturnsList.table.header.iossNumber")),
          classes = "govuk-!-width-one-quarter"
        )
      )),
      caption = Some(messages("clientReturnsList.active.heading")),
      captionClasses = "govuk-table__caption govuk-table__caption--m"
    )
  }

  private def excludedClientReturnsTable(
                                          excludedClients: Seq[EtmpClientDetails],
                                          redirectUrl: String
                                        )(implicit messages: Messages): Table = {

    val excludedClientsRows = excludedClients.map { excludedClient =>
      excludedClientReturnsRows(excludedClient, redirectUrl)
    }

    Table(
      rows = excludedClientsRows,
      head = Some(Seq(
        HeadCell(
          content = Text(messages("clientReturnsList.table.header.clientName")),
          classes = "govuk-!-width-three-quarters"
        ),
        HeadCell(
          content = Text(messages("clientReturnsList.table.header.iossNumber")),
          classes = "govuk-!-width-one-quarter"
        )
      )),
      caption = Some(messages("clientReturnsList.excluded.heading")),
      captionClasses = "govuk-table__caption govuk-table__caption--m govuk-!-margin-top-6"
    )
  }
}