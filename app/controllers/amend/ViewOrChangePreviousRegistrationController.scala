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

package controllers.amend

import config.FrontendAppConfig
import controllers.actions.AuthenticatedControllerComponents
import logging.Logging
import pages.Waypoints
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import queries.PreviousRegistrationIntermediaryNumberQuery
import services.intermediaries.AccountService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
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
                                                          )(implicit ec: ExecutionContext)
  extends FrontendBaseController with I18nSupport with Logging {

  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad(waypoints: Waypoints): Action[AnyContent] =
    cc.identifyGetDataAndRegistration.async { implicit request =>

      val intermediaryNumber: String = request.userAnswers
        .flatMap(_.get(PreviousRegistrationIntermediaryNumberQuery))
        .getOrElse {
          throw new IllegalStateException("Intermediary number missing")
        }

      accountService.getRegistrationClientDetails(intermediaryNumber).map {

        case Right(clientDetailsList) =>

          val changeRegistrationRedirectUrl = frontendAppConfig.changeYourNetpRegistrationUrl
          val returnToCurrentRegUrl: String = frontendAppConfig.viewClientsListUrl

          val viewModel = ViewOrChangePreviousRegistrationViewModel(
            clientDetailsList,
            changeRegistrationRedirectUrl
          )

          Ok(view(waypoints, intermediaryNumber, viewModel, returnToCurrentRegUrl))

        case Left(error) =>
          logger.error(s"Failed to retrieve client details: $error")
          InternalServerError
      }

    }

}
