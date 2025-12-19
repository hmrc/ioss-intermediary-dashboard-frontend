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
import connectors.{RegistrationConnector, SecureMessageConnector}
import controllers.actions.*
import logging.Logging
import models.etmp.EtmpExclusion
import models.etmp.EtmpExclusionReason.*
import models.returns.{CurrentReturns, SubmissionStatus}
import pages.Waypoints
import pages.saveForLater.{ContinueSingleClientSavedReturnPage, SelectClientSavedReturnPage}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SaveForLaterService
import services.returns.CurrentReturnsService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.FutureSyntax.FutureOps
import viewmodels.dashboard.DashboardUrlsViewModel
import views.html.YourAccountView

import java.time.{Clock, LocalDate}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class YourAccountController @Inject()(
                                       cc: AuthenticatedControllerComponents,
                                       view: YourAccountView,
                                       registrationConnector: RegistrationConnector,
                                       secureMessageConnector: SecureMessageConnector,
                                       saveForLaterService: SaveForLaterService,
                                       currentReturnsService: CurrentReturnsService,
                                       appConfig: FrontendAppConfig,
                                       clock: Clock
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging {

  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad(waypoints: Waypoints): Action[AnyContent] = cc.identifyAndGetRegistration.async {
    implicit request =>

      val vrn = request.vrn.vrn
      val intermediaryEnrolment = appConfig.intermediaryEnrolment

      registrationConnector.getNumberOfSavedUserAnswers(request.intermediaryNumber).flatMap { numberOfSavedUserJourneys =>
        registrationConnector.getNumberOfPendingRegistrations(request.intermediaryNumber).map(_.toInt).flatMap { numberOfAwaitingClients =>
          registrationConnector.getVatCustomerInfo(vrn).flatMap {
            case Right(vatInfo) =>

              secureMessageConnector.getMessages(taxIdentifiers = Some(intermediaryEnrolment)).flatMap {
                case Right(secureMessages) =>

                  registrationConnector.displayRegistration(request.intermediaryNumber).flatMap {
                    case Right(registrationWrapper) =>

                      val businessName = vatInfo.organisationName.orElse(vatInfo.individualName).getOrElse("")
                      val intermediaryNumber = request.intermediaryNumber

                      val maybeExclusion: Option[EtmpExclusion] = request.registrationWrapper.etmpDisplayRegistration.exclusions.lastOption
                      val leaveThisServiceUrl = if (maybeExclusion.isEmpty || maybeExclusion.exists(_.exclusionReason == Reversal)) {
                        Some(appConfig.leaveThisServiceUrl)
                      } else {
                        None
                      }

                      val messageCount = secureMessages.count.unread match {
                        case unread if unread > 0 => unread.toInt
                        case _ => secureMessages.count.total.toInt
                      }

                      val hasUnreadMessages = if (secureMessages.count.unread > 0) true else false

                      val currentDate: LocalDate = LocalDate.now(clock)
                      val isRejoinEligible = registrationWrapper.etmpDisplayRegistration.canRejoinScheme(currentDate)

                      val futureFinalReturnComplete = if (isRejoinEligible) {
                        currentReturnsService.getCurrentReturns(intermediaryNumber).map { hasOutstandingReturns =>
                          getExistingOutstandingReturns(hasOutstandingReturns)
                        }
                      } else {
                        false.toFuture
                      }

                      futureFinalReturnComplete.flatMap { finalReturnComplete =>
                        checkIntermediarySavedAnswersAndRedirect(waypoints).flatMap { redirectUrl =>

                          val urls = DashboardUrlsViewModel(
                            addClientUrl = appConfig.addClientUrl,
                            viewClientReturnsListUrl = controllers.routes.ClientReturnsListController.onPageLoad().url,
                            viewClientsListUrl = controllers.routes.ClientListController.onPageLoad().url,
                            changeYourRegistrationUrl = appConfig.changeYourRegistrationUrl,
                            pendingClientsUrl = controllers.routes.ClientAwaitingActivationController.onPageLoad().url,
                            secureMessagesUrl = controllers.routes.SecureMessagesController.onPageLoad().url,
                            leaveThisServiceUrl = leaveThisServiceUrl,
                            continueSavedRegUrl = appConfig.continueRegistrationUrl,
                            rejoinSchemeUrl = appConfig.rejoinSchemeUrl,
                            makeAPaymentUrl = controllers.routes.PaymentsClientListController.onPageLoad().url,
                            startClientCurrentReturnsUrl = controllers.returns.routes.ClientsOutstandingReturnsListController.onPageLoad(waypoints).url,
                            continueSavedReturnUrl = redirectUrl
                          )

                          Ok(view(
                            businessName,
                            intermediaryNumber,
                            messageCount,
                            hasUnreadMessages,
                            numberOfAwaitingClients,
                            cancelYourRequestToLeaveUrl(maybeExclusion),
                            numberOfSavedUserJourneys,
                            urls,
                            isRejoinEligible,
                            finalReturnComplete
                          )).toFuture
                        }
                      }

                    case Left(error) =>
                      val message: String = s"Received an unexpected error when trying to retrieve registration details: $error."
                      val exception: Exception = new Exception(message)
                      logger.error(exception.getMessage, exception)
                      throw exception

                  }

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

  private def cancelYourRequestToLeaveUrl(maybeExclusion: Option[EtmpExclusion]): Option[String] = {
    maybeExclusion match {
      case Some(exclusion) if Seq(VoluntarilyLeaves, TransferringMSID).contains(exclusion.exclusionReason) &&
        LocalDate.now(clock).isBefore(exclusion.effectiveDate) =>
        Some(appConfig.cancelYourRequestToLeaveUrl)
      case _ => None
    }
  }

  private def getExistingOutstandingReturns(currentReturns: Seq[CurrentReturns]): Boolean = {
    currentReturns.exists { cr =>
      if (cr.finalReturnsCompleted) {
        false
      } else {
        cr.incompleteReturns.exists { currentReturns =>
          Seq(SubmissionStatus.Due, SubmissionStatus.Overdue, SubmissionStatus.Next).contains(currentReturns.submissionStatus)
        }
      }
    }
  }

  private def checkIntermediarySavedAnswersAndRedirect(
                                                        waypoints: Waypoints
                                                      )(implicit hc: HeaderCarrier): Future[Option[String]] = {

    for {
      savedUserAnswers <- saveForLaterService.getAllClientSavedAnswers()
    } yield {
      savedUserAnswers match {
        case Nil =>
          None

        case saveAnswers :: Nil =>
          val iossNumber: String = saveAnswers.iossNumber
          Some(ContinueSingleClientSavedReturnPage(iossNumber).route(waypoints).url)

        case _ =>
          Some(SelectClientSavedReturnPage.route(waypoints).url)
      }
    }
  }
}
