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

package controllers.returns

import config.FrontendAppConfig
import controllers.actions.*
import controllers.returns.GetOutstandingClientDetails.getOutstandingClientDetailsForStatus
import models.etmp.EtmpClientDetails
import models.returns.CurrentReturns
import models.returns.SubmissionStatus.{Due, Overdue}
import pages.Waypoints
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.returns.CurrentReturnsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.returns.ClientOutstandingReturnsListViewModel
import views.html.returns.ClientsOutstandingReturnsListView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class ClientsOutstandingReturnsListController @Inject()(
                                                         override val messagesApi: MessagesApi,
                                                         cc: AuthenticatedControllerComponents,
                                                         currentReturnsService: CurrentReturnsService,
                                                         frontendAppConfig: FrontendAppConfig,
                                                         view: ClientsOutstandingReturnsListView
                                                       )(implicit ec: ExecutionContext)
  extends FrontendBaseController with I18nSupport with Logging {

  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad(waypoints: Waypoints): Action[AnyContent] = cc.identifyAndGetRegistration.async {
    implicit request =>

      val clientDetails: Seq[EtmpClientDetails] = request.registrationWrapper.etmpDisplayRegistration.clientDetails

      currentReturnsService.getCurrentReturns(request.intermediaryNumber).map { currentReturns =>
        val clientIossNumbersWithDueAndOverdueReturns: Seq[EtmpClientDetails] = getOutstandingClientDetails(currentReturns, clientDetails)

        val startReturnUrl: String = frontendAppConfig.startCurrentReturnUrl
        val viewModel: ClientOutstandingReturnsListViewModel =
          ClientOutstandingReturnsListViewModel(clientIossNumbersWithDueAndOverdueReturns, startReturnUrl)

        val viewOverdueReturnsLink: String = routes.ClientsOverdueReturnsListController.onPageLoad(waypoints).url

        val currentReturnsNonEmpty: Boolean = clientIossNumbersWithDueAndOverdueReturns.nonEmpty
        val overdueReturnsNonEmpty: Boolean = getOutstandingClientDetailsForStatus(currentReturns, Overdue, clientDetails).nonEmpty
        Ok(view(viewModel, viewOverdueReturnsLink, currentReturnsNonEmpty, overdueReturnsNonEmpty))
      }
  }

  private def getOutstandingClientDetails(
                                           currentReturns: Seq[CurrentReturns],
                                           clientDetails: Seq[EtmpClientDetails]
                                         ): Seq[EtmpClientDetails] = {
    val iossNumbersWithDueReturns: Seq[String] = currentReturns.filter { currentReturn =>
      currentReturn.incompleteReturns.exists { clientReturn =>
        clientReturn.submissionStatus == Due || clientReturn.submissionStatus == Overdue
      }
    }.map(_.iossNumber)

    clientDetails.filter { clientDetails =>
      iossNumbersWithDueReturns.contains(clientDetails.clientIossID)
    }
  }
}
