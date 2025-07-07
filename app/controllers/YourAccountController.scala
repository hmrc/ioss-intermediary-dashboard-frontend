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
import controllers.actions.*
import logging.Logging
import models.responses.VatCustomerNotFound

import javax.inject.Inject
import pages.{JourneyRecoveryPage, Waypoints}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.YourAccountView
import utils.FutureSyntax.FutureOps

import scala.concurrent.{ExecutionContext, Future}

class YourAccountController @Inject()(
                                       cc: AuthenticatedControllerComponents,
                                       view: YourAccountView,
                                       registrationConnector: RegistrationConnector,
                                       appConfig: FrontendAppConfig
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging {

  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad(waypoints: Waypoints): Action[AnyContent] = cc.identify.async {
    implicit request =>

      val vrn = request.vrn.vrn
      registrationConnector.getVatCustomerInfo(vrn).flatMap {
        case Right(vatInfo) =>
          val businessName = vatInfo.organisationName.orElse(vatInfo.individualName).getOrElse("")
          val intermediaryNumber = request.intermediaryNumber
          val newMessages = 0
          val addClientUrl = appConfig.addClientUrl

          Ok(view(
            waypoints,
            businessName,
            intermediaryNumber,
            newMessages,
            addClientUrl
          )).toFuture

        case Left(_) =>
          logger.error("Vat Info Not Found")
          Redirect(JourneyRecoveryPage.route(waypoints)).toFuture

      }
  }
}
