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

import config.FrontendAppConfig
import connectors.RegistrationConnector
import connectors.test.TestOnlySecureMessagingConnector
import controllers.actions.*
import logging.Logging
import models.etmp.EtmpExclusion
import models.etmp.EtmpExclusionReason.*

import javax.inject.Inject
import pages.Waypoints
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.YourAccountView
import utils.FutureSyntax.FutureOps

import java.time.{Clock, LocalDate}
import scala.concurrent.ExecutionContext

class YourAccountController @Inject()(
                                       cc: AuthenticatedControllerComponents,
                                       view: YourAccountView,
                                       registrationConnector: RegistrationConnector,
                                       testOnlySecureMessagingConnector: TestOnlySecureMessagingConnector,
                                       appConfig: FrontendAppConfig,
                                       clock: Clock
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging {

  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad(waypoints: Waypoints): Action[AnyContent] = cc.identifyAndGetRegistration.async {
    implicit request =>

      val vrn = request.vrn.vrn
      val maybeExclusion: Option[EtmpExclusion] = request.registrationWrapper.etmpDisplayRegistration.exclusions.lastOption
      val leaveThisServiceUrl = if (maybeExclusion.isEmpty || maybeExclusion.exists(_.exclusionReason == Reversal)) {
        Some(appConfig.leaveThisServiceUrl)
      } else {
        None
      }
      registrationConnector.getNumberOfSavedUserAnswers(request.intermediaryNumber).flatMap { numberOfSavedUserJourneys =>
        registrationConnector.getNumberOfPendingRegistrations(request.intermediaryNumber).map(_.toInt).flatMap { numberOfAwaitingClients =>
          registrationConnector.getVatCustomerInfo(vrn).flatMap {
            case Right(vatInfo) =>
              val businessName = vatInfo.organisationName.orElse(vatInfo.individualName).getOrElse("")
              val intermediaryNumber = request.intermediaryNumber

            testOnlySecureMessagingConnector.getMessages(taxIdentifiers = Some("HMRC-IOSS-INT")).flatMap {
              case Right(secureMessages) =>

                val messagesCount = secureMessages.count.total.toInt
                val addClientUrl = appConfig.addClientUrl
                val changeYourRegistrationUrl = appConfig.changeYourRegistrationUrl
                val redirectToPendingClientsPage = appConfig.redirectToPendingClientsPage
                val redirectToSecureMessagesPage = appConfig.redirectToSecureMessagesPage
                val leaveThisServiceUrl = appConfig.leaveThisServiceUrl
                val viewClientsListUrl: String = appConfig.viewClientsListUrl
                val continueSavedRegUrl = appConfig.continueRegistrationUrl

                Ok(view(
                  waypoints,
                  businessName,
                  intermediaryNumber,
                  messagesCount,
                  addClientUrl,
                  viewClientsListUrl,
                  changeYourRegistrationUrl,
                  numberOfAwaitingClients,
                  redirectToPendingClientsPage,
                  redirectToSecureMessagesPage,
                  leaveThisServiceUrl,
                  cancelYourRequestToLeaveUrl(maybeExclusion),
                  numberOfSavedUserJourneys,
                  continueSavedRegUrl
                )).toFuture

              case Left(errors) =>
                val message: String = s"Received an unexpected error when trying to retrieve secure messages: $errors."
                val exception: Exception = new Exception(message)
                logger.error(exception.getMessage, exception)
                throw exception
            }

          case Left(error) =>
            val exception = new Exception(error.body)
            logger.error(exception.getMessage, exception)
            throw exception
        }
      }
  }
}

  private  def cancelYourRequestToLeaveUrl(maybeExclusion: Option[EtmpExclusion]): Option[String] = {
    maybeExclusion match {
      case Some(exclusion) if Seq(VoluntarilyLeaves, TransferringMSID).contains(exclusion.exclusionReason) &&
        LocalDate.now(clock).isBefore(exclusion.effectiveDate)  =>
        Some(appConfig.cancelYourRequestToLeaveUrl)
      case _ => None
    }
  }
}
