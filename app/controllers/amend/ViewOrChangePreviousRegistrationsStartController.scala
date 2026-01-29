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

import controllers.actions.*
import logging.Logging
import models.UserAnswers
import pages.Waypoints
import pages.amend.{ViewOrChangePreviousRegistrationPage, ViewOrChangePreviousRegistrationsMultiplePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import queries.PreviousRegistrationIntermediaryNumberQuery
import services.intermediaries.AccountService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ViewOrChangePreviousRegistrationsStartController @Inject()(
                                                                  override val messagesApi: MessagesApi,
                                                                  cc: AuthenticatedControllerComponents,
                                                                  accountService: AccountService,
                                                                )(implicit ec: ExecutionContext)
  extends FrontendBaseController with I18nSupport with Logging {

  override protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad(waypoints: Waypoints): Action[AnyContent] =
    cc.identifyGetDataAndRegistration.async { implicit request =>

      accountService.getPreviousRegistrations().flatMap { previousRegistrations =>

        previousRegistrations.size match {

          case 0 =>
            val exception =
              new IllegalStateException("Must have one or more previous registrations")
            logger.error(exception.getMessage, exception)
            throw exception

          case 1 =>
            val intermediaryNumber: String = previousRegistrations.head.intermediaryNumber
            val baseAnswers = request.userAnswers.getOrElse(UserAnswers(request.userId))

            for {
              updatedAnswers <- Future.fromTry(
                baseAnswers.set(
                  PreviousRegistrationIntermediaryNumberQuery,
                  intermediaryNumber
                )
              )
              _ <- cc.sessionRepository.set(updatedAnswers)
            } yield Redirect(
              ViewOrChangePreviousRegistrationPage.route(waypoints).url
            )

          case _ =>
            Future.successful(
              Redirect(
                ViewOrChangePreviousRegistrationsMultiplePage.route(waypoints).url
              )
            )
        }
      }
    }
}



