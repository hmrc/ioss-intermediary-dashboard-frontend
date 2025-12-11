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
import controllers.actions.AuthenticatedControllerComponents
import logging.Logging
import pages.Waypoints
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.ioss.AccountService
import services.returns.CurrentReturnsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.returns.ClientOutstandingReturnsListViewModel
import views.html.returns.ClientsPreviousRegistrationReturnsListView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class ClientsPreviousRegistrationReturnsListController @Inject()(
                                                                 override val messagesApi: MessagesApi,
                                                                 cc: AuthenticatedControllerComponents,
                                                                 accountService: AccountService,
                                                                 frontendAppConfig: FrontendAppConfig,
                                                                 currentReturnsService: CurrentReturnsService,
                                                                 view: ClientsPreviousRegistrationReturnsListView,
                                                                 val controllerComponents: MessagesControllerComponents,
                                                               )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging {
  
  def onPageLoad(waypoints: Waypoints): Action[AnyContent] = cc.identifyAndGetRegistration.async {
    implicit request =>
      
      accountService.getPreviousRegistrations().flatMap { previousRegistrations =>
        
        previousRegistrations.size match {
          case 0 =>
            val exception = new IllegalStateException("Must have one or more previous registrations")
            logger.error(exception.getMessage, exception)
            throw exception

          case _ =>
            
            val clientDetails = request.registrationWrapper.etmpDisplayRegistration.clientDetails
            
            currentReturnsService.getCurrentReturns(request.intermediaryNumber).map { currentReturns =>
              
              val startPreviousRegistrationReturnsHistoryUrl: String = frontendAppConfig.startPreviousRegistrationReturnsHistoryUrl
              
              val viewModel = ClientOutstandingReturnsListViewModel(
                clientDetails,
                startPreviousRegistrationReturnsHistoryUrl
              )

              val navigateToCurrentReturnsUrl = routes.ClientReturnsListController.onPageLoad(waypoints).url
              
              Ok(view(viewModel, request.intermediaryNumber, navigateToCurrentReturnsUrl, startPreviousRegistrationReturnsHistoryUrl))
            }
        }
      }
  }
}
