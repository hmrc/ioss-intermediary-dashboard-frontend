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

package viewmodels.returns

import models.etmp.EtmpClientDetails
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Table
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.{HeadCell, TableRow}
import viewmodels.LinkModel

case class ClientOutstandingReturnsListViewModel(clientOutstandingReturns: Table)

object ClientOutstandingReturnsListViewModel {

  def apply(
             clientsOutstanding: Seq[EtmpClientDetails],
             redirectUrl: String
           )(implicit messages: Messages): ClientOutstandingReturnsListViewModel = {

    ClientOutstandingReturnsListViewModel(
      clientOutstandingReturns = clientOutstandingReturnsTable(clientsOutstanding, redirectUrl)
    )
  }

  private def clientOutstandingReturnsRows(
                                            clientDetails: EtmpClientDetails,
                                            redirectUrl: String
                                          )(implicit messages: Messages): Seq[TableRow] = {
    Seq(
      TableRow(
        content = HtmlContent(
          messages(
            "clientsReturnsListCommon.table.startClientReturn.link",
            clientDetails.clientName,
            startReturnLink(clientDetails.clientIossID, redirectUrl).url,
            messages("clientsReturnsListCommon.table.startClientReturn.hidden", clientDetails.clientName)
          )
        ),
        classes = "govuk-table__cell govuk-!-font-weight-regular"
      ),
      TableRow(
        content = HtmlContent(
          clientDetails.clientIossID
        ),
        classes = "govuk-table__cell govuk-!-font-weight-regular"
      )
    )
  }

  private def clientOutstandingReturnsTable(
                                             allOutstandingClientDetails: Seq[EtmpClientDetails],
                                             redirectUrl: String
                                           )(implicit messages: Messages): Table = {

    val outstandingClientRows: Seq[Seq[TableRow]] = allOutstandingClientDetails
      .sortBy(_.clientName).map { clientDetails =>
        clientOutstandingReturnsRows(clientDetails, redirectUrl)
      }

    Table(
      rows = outstandingClientRows,
      head = Some(Seq(
        HeadCell(
          content = Text(messages("clientsReturnsListCommon.table.header.clientName")),
          classes = "govuk-table__header govuk-!-width-two-thirds"
        ),
        HeadCell(
          content = Text(messages("clientsReturnsListCommon.table.header.iossNumber")),
          classes = "govuk-table__header govuk-!-width-one-half"
        )
      ))
    )
  }

  private def startReturnLink(clientIossNumber: String, redirectUrl: String)(implicit messages: Messages) = {
    LinkModel(
      linkText = messages("yourAccount.returns.startReturn"),
      id = "start-your-return",
      url = s"$redirectUrl/$clientIossNumber"
    )
  }
}
