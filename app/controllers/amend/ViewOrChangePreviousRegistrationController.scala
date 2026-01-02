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

package controllers.amend

import config.FrontendAppConfig
import controllers.actions.*
import logging.Logging
import models.etmp.EtmpClientDetails
import models.responses.ErrorResponse
import pages.amend.ViewOrChangePreviousRegistrationsMultiplePage
import pages.{Waypoints, YourAccountPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.intermediaries.AccountService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.FutureSyntax.FutureOps
import viewmodels.amend.ViewOrChangePreviousRegistrationViewModel
import views.html.ViewOrChangePreviousRegistrationView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class ViewOrChangePreviousRegistrationController @Inject()(
                                                            override val messagesApi: MessagesApi,
                                                            cc: AuthenticatedControllerComponents,
                                                            accountService: AccountService,
                                                            view: ViewOrChangePreviousRegistrationView,
                                                            frontendAppConfig: FrontendAppConfig,
                                                          )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging {

  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad(waypoints: Waypoints): Action[AnyContent] =
    cc.identifyAndGetRegistration.async {
      implicit request =>
        accountService.getPreviousRegistrations().flatMap { previousRegistrations =>

          previousRegistrations.size match {
            case 0 =>
              val exception = new IllegalStateException("Must have one or more previous registrations")
              logger.error(exception.getMessage, exception)
              throw exception

            case 1 =>
              val intermediaryNumber: String = previousRegistrations.map(_.intermediaryNumber).head

              val returnToCurrentRegUrl: String = frontendAppConfig.viewClientsListUrl

              accountService.getRegistrationClientDetails(intermediaryNumber).map {
                case Right(clientDetailsList) =>
                  val changeRegistrationRedirectUrl = YourAccountPage.route(waypoints).url // TODO: Update link

                  val viewModel = ViewOrChangePreviousRegistrationViewModel(
                    clientDetailsList,
                    changeRegistrationRedirectUrl
                  )


                  Ok(view(waypoints, intermediaryNumber, viewModel, returnToCurrentRegUrl))

                case Left(error) =>
                  logger.error(s"Failed to retrieve client details: $error")
                  InternalServerError
              }

            case _ =>
              Redirect(ViewOrChangePreviousRegistrationsMultiplePage.route(waypoints).url).toFuture
          }
        }
    }
}


