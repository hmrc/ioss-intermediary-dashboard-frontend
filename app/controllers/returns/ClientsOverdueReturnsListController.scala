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

package controllers.returns

import config.FrontendAppConfig
import controllers.actions.*
import controllers.returns.GetOutstandingClientDetails.getOutstandingClientDetailsForStatus
import models.etmp.EtmpClientDetails
import models.returns.SubmissionStatus.Overdue
import pages.Waypoints
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.returns.CurrentReturnsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.returns.ClientOutstandingReturnsListViewModel
import views.html.returns.ClientsOverdueReturnsListView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class ClientsOverdueReturnsListController @Inject()(
                                                     override val messagesApi: MessagesApi,
                                                     cc: AuthenticatedControllerComponents,
                                                     currentReturnsService: CurrentReturnsService,
                                                     frontendAppConfig: FrontendAppConfig,
                                                     view: ClientsOverdueReturnsListView
                                                   )(implicit ec: ExecutionContext)
  extends FrontendBaseController with I18nSupport with Logging {

  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad(waypoints: Waypoints): Action[AnyContent] = cc.identifyAndGetRegistration.async {
    implicit request =>

      currentReturnsService.getCurrentReturns(request.intermediaryNumber).map { currentReturns =>
        val clientDetailsWithOverdueReturns: Seq[EtmpClientDetails] = getOutstandingClientDetailsForStatus(
          currentReturns, Overdue, request.registrationWrapper.etmpDisplayRegistration.clientDetails
        )

        val viewOverdueReturnsLink: String = routes.ClientsOutstandingReturnsListController.onPageLoad(waypoints).url
        val startReturnUrl: String = frontendAppConfig.startCurrentReturnUrl
        val viewModel: ClientOutstandingReturnsListViewModel =
          ClientOutstandingReturnsListViewModel(clientDetailsWithOverdueReturns, startReturnUrl)

        val currentReturnsNonEmpty: Boolean = clientDetailsWithOverdueReturns.nonEmpty
        Ok(view(viewModel, viewOverdueReturnsLink, currentReturnsNonEmpty))
      }
  }
}
