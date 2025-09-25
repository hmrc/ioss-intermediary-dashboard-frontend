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
import models.etmp.EtmpClientDetails
import pages.Waypoints
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ClientListView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class ClientListController @Inject()(
                                      override val messagesApi: MessagesApi,
                                      cc: AuthenticatedControllerComponents,
                                      registrationConnector: RegistrationConnector,
                                      view: ClientListView
                                    )(implicit executionContext: ExecutionContext)
  extends FrontendBaseController with I18nSupport with Logging {

  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad(waypoints: Waypoints): Action[AnyContent] = cc.identify.async {
    implicit request =>

      for {
        displayRegistrationResponse <- registrationConnector.getDisplayRegistration(request.intermediaryNumber)
      } yield {
        val clientDetailsList: Seq[EtmpClientDetails] = displayRegistrationResponse match {
          case Right(clientDetails) => clientDetails

          case Left(error) =>
            val errorMessage: String = s"There was a problem retrieving Client Details with error: ${error.body}"
            logger.error(errorMessage)
            throw new Exception(errorMessage)
        }

        val activeClientList: Seq[EtmpClientDetails] = clientDetailsList.filterNot(_.clientExcluded)
        val excludedClientList: Seq[EtmpClientDetails] = clientDetailsList.filter(_.clientExcluded)

        val changeRegistrationRedirectUrl: String = "" // TODO -> Redirect to changeReg for given ioss number?
        val excludeClientRedirectUrl: String = "" // TODO -> Redirect to exclude for given ioss number?

        Ok(view(activeClientList, excludedClientList, changeRegistrationRedirectUrl, excludeClientRedirectUrl))
      }
  }
}
