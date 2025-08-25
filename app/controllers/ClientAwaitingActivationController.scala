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

import connectors.RegistrationConnector
import controllers.actions.*
import logging.Logging

import javax.inject.Inject
import pages.Waypoints
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.govukfrontend.views.Aliases.Table
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.{HeadCell, TableRow}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ClientAwaitingActivationView
import utils.FutureSyntax.FutureOps

import scala.concurrent.ExecutionContext

class ClientAwaitingActivationController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       cc: AuthenticatedControllerComponents,
                                       val controllerComponents: MessagesControllerComponents,
                                       registrationConnector: RegistrationConnector,
                                       view: ClientAwaitingActivationView
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging {

  def onPageLoad(waypoints: Waypoints): Action[AnyContent] = (cc.actionBuilder andThen cc.identify).async {
    implicit request =>

      registrationConnector.getNumberOfPendingRegistration(request.intermediaryNumber).map(_.toInt).flatMap { numberOfAwaitingClients =>
        registrationConnector.getPendingRegistration(request.intermediaryNumber).flatMap {
          case Right(savedPendingRegistration) =>
            val companyName = savedPendingRegistration.userAnswers.vatInfo.get.organisationName.getOrElse("")
            val activationExpiryDate = savedPendingRegistration.activationExpiryDate

            val clientsTable =
              buildClientsTable(companyName, activationExpiryDate, request.userId)

            Ok(view(numberOfAwaitingClients, clientsTable)).toFuture

          case Left(errors) =>
            val message: String = s"Received an unexpected error when trying to retrieve a pending registration for the given intermediary number: $errors."
            val exception: Exception = new Exception(message)
            logger.error(exception.getMessage, exception)
            throw exception
        }
      }
  }

  private def buildClientsTable(clientCompanyName: String, activationExpiryDate: String, userId: String)(implicit messages: Messages): Table =
    /*
    Add a for loop (or recursion) to iterate over the number of clients and render them in the table.
     */
    Table(
      rows = Seq(
        Seq(
          TableRow(
            content = HtmlContent(messages("clientAwaitingActivation.name", clientCompanyName, userId))
          ),
          TableRow(
            content = Text(activationExpiryDate)
          )
        )
      ),
      head = Some(Seq(
        HeadCell(
          content = Text("Client name")
        ),
        HeadCell(
          content = Text("Expiry date")
        )
      ))
    )

}
