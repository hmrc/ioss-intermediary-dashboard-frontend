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

import controllers.actions.*
import models.etmp.EtmpClientDetails

import javax.inject.Inject
import pages.Waypoints
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.ClientReturnService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.clientList.ClientReturnsListViewModel
import views.html.ClientReturnsListView

import scala.concurrent.ExecutionContext

class ClientReturnsListController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       cc: AuthenticatedControllerComponents,
                                       val controllerComponents: MessagesControllerComponents,
                                       clientReturnService: ClientReturnService,
                                       view: ClientReturnsListView
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(waypoints: Waypoints): Action[AnyContent] = cc.identifyAndGetRegistration.async {
    implicit request =>
      val clientDetailsList: Seq[EtmpClientDetails] = request.registrationWrapper.etmpDisplayRegistration.clientDetails
      val intermediaryNumber = request.intermediaryNumber

      clientReturnService.clientsWithCompletedReturns(clientDetailsList, intermediaryNumber).map {
        filteredClients =>

          val viewModel: ClientReturnsListViewModel = ClientReturnsListViewModel(filteredClients)

          Ok(view(viewModel))
      }

  }
}
