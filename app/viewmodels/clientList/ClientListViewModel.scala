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

case class ClientListViewModel(
                                activeClients: Table,
                                excludedClients: Table
                              )

object ClientListViewModel {

  def apply(
             clientList: Seq[EtmpClientDetails],
             changeClientRegistrationUrl: String,
             excludeClientUrl: String
           )(implicit messages: Messages): ClientListViewModel = {

    val activeClients: Seq[EtmpClientDetails] = clientList.filterNot(_.clientExcluded)
    val excludedClients: Seq[EtmpClientDetails] = clientList.filter(_.clientExcluded)


    ClientListViewModel(
      activeClients = activeClientTable(activeClients, changeClientRegistrationUrl, excludeClientUrl),
      excludedClients = excludedClientTable(excludedClients, changeClientRegistrationUrl)
    )
  }

  private def activeClientRows(
                                activeClient: EtmpClientDetails,
                                changeClientRegistrationUrl: String,
                                excludeClientUrl: String
                              )(implicit messages: Messages): Seq[TableRow] = {
    Seq(
      TableRow(
        content = HtmlContent(
          messages(
            "clientList.change.link",
            activeClient.clientName,
            s"$changeClientRegistrationUrl/${activeClient.clientIossID}",
            messages("clientList.change.hidden", activeClient.clientName)
          )
        ),
        classes = "govuk-!-font-weight-regular"
      ),
      TableRow(
        content = Text(activeClient.clientIossID)
      ),
      TableRow(
        content = HtmlContent(
          messages(
            "clientList.remove.link",
            messages("site.remove"),
            s"$excludeClientUrl/${activeClient.clientIossID}",
            messages("clientList.remove.hidden", activeClient.clientName)
          )
        ),
        classes = "govuk-table__cell--numeric"
      )
    )
  }

  private def excludedClientRows(
                                  excludedClient: EtmpClientDetails,
                                  changeClientRegistrationUrl: String
                                )(implicit messages: Messages): Seq[TableRow] = {
    Seq(
      TableRow(
        content = HtmlContent(
          messages(
            "clientList.change.link",
            excludedClient.clientName,
            s"$changeClientRegistrationUrl/${excludedClient.clientIossID}",
            messages("clientList.change.hidden", excludedClient.clientName)
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

  private def activeClientTable(
                                 activeClients: Seq[EtmpClientDetails],
                                 changeClientRegistrationUrl: String,
                                 excludeClientUrl: String
                               )(implicit messages: Messages): Table = {

    val activeClientsRows = activeClients.map { activeClient =>
      activeClientRows(activeClient, changeClientRegistrationUrl, excludeClientUrl)
    }

    Table(
      rows = activeClientsRows,
      head = Some(Seq(
        HeadCell(
          content = Text(messages("clientList.table.header.clientName"))
        ),
        HeadCell(
          content = Text(messages("clientList.table.header.iossNumber")),
          classes = "govuk-!-width-one-third"
        ),
        HeadCell(
          content = Text(messages("table.header.action")),
          classes = "govuk-table__header--numeric govuk-!-width-one-third"
        )
      )),
      caption = Some(messages("clientList.active.heading")),
      captionClasses = "govuk-table__caption govuk-table__caption--m"
    )
  }

  private def excludedClientTable(
                                   excludedClients: Seq[EtmpClientDetails],
                                   changeClientRegistrationUrl: String
                                 )(implicit messages: Messages): Table = {

    val excludedClientsRows = excludedClients.map { excludedClient =>
      excludedClientRows(excludedClient, changeClientRegistrationUrl)
    }

    Table(
      rows = excludedClientsRows,
      head = Some(Seq(
        HeadCell(
          content = Text(messages("clientList.table.header.clientName"))
        ),
        HeadCell(
          content = Text(messages("clientList.table.header.iossNumber")),
          classes = "govuk-!-width-two-thirds"
        )
      )),
      caption = Some(messages("clientList.excluded.heading")),
      captionClasses = "govuk-table__caption govuk-table__caption--m govuk-!-margin-top-6"
    )
  }
}
