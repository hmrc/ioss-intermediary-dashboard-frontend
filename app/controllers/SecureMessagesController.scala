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

package controllers

import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.SecureMessageConnector
import controllers.actions.AuthenticatedControllerComponents
import logging.Logging
import models.securemessage.{CustomerEnrolment, MessageFilter}
import pages.Waypoints
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.govukfrontend.views.Aliases.Table
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.{HeadCell, TableRow}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.SecureMessagesView
import utils.FutureSyntax.FutureOps

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.concurrent.ExecutionContext

class SecureMessagesController @Inject()(
                                          override val messagesApi: MessagesApi,
                                          val controllerComponents: MessagesControllerComponents,
                                          cc: AuthenticatedControllerComponents,
                                          secureMessageConnector: SecureMessageConnector,
                                          frontendAppConfig: FrontendAppConfig,
                                          view: SecureMessagesView
                                        )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging {

  def onPageLoad(waypoints: Waypoints): Action[AnyContent] = cc.actionBuilder.async {
    implicit request =>

      val intermediaryEnrolment = frontendAppConfig.intermediaryEnrolment

      secureMessageConnector.getMessages(taxIdentifiers = Some(intermediaryEnrolment)).flatMap {
        case Right(secureMessages) =>

          val unreadMessages: Seq[Boolean] = secureMessages.items.map(_.unreadMessages)
          val messageSubject: Seq[String] = secureMessages.items.map(_.subject)
          val messageValidFrom: Seq[String] = secureMessages.items.map(_.validFrom)

          val messagesTable = buildMessagesTable(messageSubject, messageValidFrom, unreadMessages)

          Ok(view(messagesTable)).toFuture

        case Left(errors) =>
          val message: String = s"Received an unexpected error when trying to retrieve secure messages: $errors."
          val exception: Exception = new Exception(message)
          logger.error(exception.getMessage, exception)
          throw exception
      }
  }

  private def buildMessagesTable(
                                  subject: Seq[String],
                                  validFrom: Seq[String],
                                  unreadMessages: Seq[Boolean]
                                 )(implicit messages: Messages): Table = {

    val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")

    val rows: Seq[Seq[TableRow]] = {

      val combinedList = subject.lazyZip(validFrom).lazyZip(unreadMessages).toList

      val sortedList = combinedList.sortBy { case (_, dateStr, _) =>
        LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE)
      }(Ordering[LocalDate].reverse)

      sortedList.map { case (sub, dateStr, unreadMessage) =>
        val formattedDate = LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE).format(dateFormatter)

        val displayRedDot = if (unreadMessage) HtmlContent(messages("redDot")) else HtmlContent(messages(""))

        val displayCorrectMessage =
          if (unreadMessage) HtmlContent(messages("secureMessages.subject.unread", sub)) else HtmlContent(messages("secureMessages.subject.read", sub))

        Seq(
          TableRow(content = displayRedDot),
          TableRow(content = displayCorrectMessage),
          TableRow(content = Text(formattedDate))
        )
      }
    }

    Table(
      rows,
      head = Some(Seq(
        HeadCell(
          content = Text("")
        ),
        HeadCell(
          content = Text(messages("secureMessages.table.headContent.column1"))
        ),
        HeadCell(
          content = Text(messages("secureMessages.table.headContent.column2")),
          classes = "govuk-!-width-one-quarter"
        )
      )),
      caption = Some(messages("secureMessages.table.caption")),
      captionClasses = "govuk-table__caption--l"
    )
  }
}
