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

package viewmodels.payments

import models.etmp.EtmpClientDetails
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{Table, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.{HeadCell, TableRow}

case class ClientPaymentsListViewModel(clients: Table)

object ClientPaymentsListViewModel {

  def apply(
             clients: Seq[EtmpClientDetails],

           )(implicit messages: Messages): ClientPaymentsListViewModel = {

    ClientPaymentsListViewModel(
      clients = clientListTable(clients)
    )
  }

  private def clientListRows(
                              client: EtmpClientDetails
                            )(implicit messages: Messages): Seq[TableRow] = {

    Seq(
      TableRow(
        content = HtmlContent(
          messages(
            "paymentsClientList.paymentDetails.link",
            client.clientName,
            s"", //todo Insert Payment Link here
            messages("paymentsClientList.paymentDetails.hidden", client.clientName)
          )
        ),
        classes = "govuk-!-font-weight-regular"
      ),
      TableRow(
        content = Text(client.clientIossID)
      )
    )
  }

  private def clientListTable(
                               clients: Seq[EtmpClientDetails]
                             )(implicit messages: Messages): Table = {

    val rows = clients.map(clientListRows)

    Table(
      rows = rows,
      head = Some(Seq(
        HeadCell(
          content = Text(messages("paymentsClientList.table.header.clientName")),
          classes = "govuk-!-width-three-quarters"
        ),
        HeadCell(
          content = Text(messages("paymentsClientList.table.header.iossNumber")),
          classes = "govuk-!-width-one-quarter"
        )
      ))
    )
  }
}