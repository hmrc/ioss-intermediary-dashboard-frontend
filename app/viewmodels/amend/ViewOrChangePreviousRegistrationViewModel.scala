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

package viewmodels.amend

import models.etmp.EtmpClientDetails
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{Table, TableRow, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.{HeadCell, TableRow}

case class ViewOrChangePreviousRegistrationViewModel(clients: Table)

object ViewOrChangePreviousRegistrationViewModel {
  def apply(
             clientList: Seq[EtmpClientDetails],
             changeClientRegistrationUrl: String,
           )(implicit messages: Messages): ViewOrChangePreviousRegistrationViewModel = {

    ViewOrChangePreviousRegistrationViewModel(
      clients = clientTable(clientList, changeClientRegistrationUrl)
    )
  }
}

private def clientRows(
                        clients: EtmpClientDetails,
                        changeClientRegistrationUrl: String,
                      )(implicit messages: Messages): Seq[TableRow] = {
  Seq(
    TableRow(
      content = HtmlContent(
        messages(
          clients.clientName,
          s"$changeClientRegistrationUrl/${clients.clientIossID}",
          messages("viewOrChangePreviousRegistration.change.hidden", clients.clientName)
        )
      ),
      classes = "govuk-!-width-one-third"
    ),
    TableRow(
      content = Text(clients.clientIossID),
      classes = "govuk-!-width-one-third"
    ),
    TableRow(
      classes = "govuk-!-width-one-third"
    )
  )
}

private def clientTable(
                         clients: Seq[EtmpClientDetails],
                         changeClientRegistrationUrl: String
                       )(implicit messages: Messages): Table = {

  val clientsRows = clients.map { prevClient =>
    clientRows(prevClient, changeClientRegistrationUrl)
  }

  Table(
    rows = clientsRows,
    head = Some(Seq(
      HeadCell(
        content = Text(messages("viewOrChangePreviousRegistration.table.header.clientName")),
        classes = "govuk-!-width-one-third"
      ),
      HeadCell(
        content = Text(messages("viewOrChangePreviousRegistration.table.header.iossNumber")),
        classes = "govuk-!-width-one-third"
      ),
      HeadCell(
        classes = "govuk-!-width-one-third"
      )
    ))
  )
}
