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
import forms.ClientsPreviousRegistrationListFormProvider

import javax.inject.Inject
import models.returns.{PreviousRegistration, SelectedPreviousRegistration}
import play.api.data.Form
import repositories.SelectedPreviousRegistrationRepository
import pages.Waypoints
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.ioss.AccountService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ClientsPreviousRegistrationListView
import utils.FutureSyntax.FutureOps

import scala.concurrent.ExecutionContext

class ClientsPreviousRegistrationListController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       cc: AuthenticatedControllerComponents,
                                       formProvider: ClientsPreviousRegistrationListFormProvider,
                                       val controllerComponents: MessagesControllerComponents,
                                       accountService: AccountService,
                                       selectedPreviousRegistrationRepository: SelectedPreviousRegistrationRepository,
                                       view: ClientsPreviousRegistrationListView
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(waypoints: Waypoints): Action[AnyContent] = cc.identifyAndGetRegistration.async {
    implicit request =>

      for {
        previousRegistrations <- accountService.getPreviousRegistrations()
        selectedPreviousRegistration <- selectedPreviousRegistrationRepository.get(request.userId)
      } yield {

        val form: Form[PreviousRegistration] = formProvider(previousRegistrations)
        val preparedForm = selectedPreviousRegistration match {
          case None => form
          case Some(value) => form.fill(value.previousRegistration)
        }

        previousRegistrations match {
          case Nil => Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
          case _ :: Nil => Redirect(controllers.routes.ClientsPreviousRegistrationReturnsListController.onPageLoad())
          case _ => Ok(view(preparedForm, waypoints, previousRegistrations))
        }
      }
  }

  def onSubmit(waypoints: Waypoints): Action[AnyContent] = cc.identifyAndGetRegistration.async {
    implicit request =>

      accountService.getPreviousRegistrations().flatMap { previousRegistrations =>
        val form: Form[PreviousRegistration] = formProvider(previousRegistrations)

        form.bindFromRequest().fold(
          formWithErrors => BadRequest(view(formWithErrors, waypoints, previousRegistrations)).toFuture,
          value =>
            selectedPreviousRegistrationRepository.set(SelectedPreviousRegistration(request.userId, value)).map { _ =>
              Redirect(controllers.routes.ClientsPreviousRegistrationReturnsListController.onPageLoad())
            }
        )
      }
  }
}
