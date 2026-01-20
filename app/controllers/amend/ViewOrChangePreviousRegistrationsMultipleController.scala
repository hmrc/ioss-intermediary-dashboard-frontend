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
import forms.ViewOrChangePreviousRegistrationsMultipleFormProvider
import models.UserAnswers
import pages.Waypoints
import pages.amend.ViewOrChangePreviousRegistrationsMultiplePage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import queries.PreviousRegistrationIntermediaryNumberQuery
import services.intermediaries.AccountService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.FutureSyntax.FutureOps
import views.html.ViewOrChangePreviousRegistrationsMultipleView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ViewOrChangePreviousRegistrationsMultipleController @Inject()(
                                                                     override val messagesApi: MessagesApi,
                                                                     cc: AuthenticatedControllerComponents,
                                                                     accountService: AccountService,
                                                                     formProvider: ViewOrChangePreviousRegistrationsMultipleFormProvider,
                                                                     val controllerComponents: MessagesControllerComponents,
                                                                     view: ViewOrChangePreviousRegistrationsMultipleView
                                                                   )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {


  def onPageLoad(waypoints: Waypoints): Action[AnyContent] = cc.identifyGetDataAndRegistration.async {
    implicit request =>

      accountService.getPreviousRegistrations().flatMap { previousRegistrations =>

        val form: Form[String] = formProvider(previousRegistrations)
        val preparedForm = request.userAnswers match {
          case Some(answers) =>
            answers.get(ViewOrChangePreviousRegistrationsMultiplePage) match {
              case Some(value) => form.fill(value)
              case None => form
            }
          case None =>
            form
        }

        Ok(view(preparedForm, waypoints, previousRegistrations)).toFuture
      }
  }

  def onSubmit(waypoints: Waypoints): Action[AnyContent] = cc.identifyGetDataAndRegistration.async { implicit request =>

      accountService.getPreviousRegistrations().flatMap { previousRegistrations =>

        val form: Form[String] = formProvider(previousRegistrations)

        form.bindFromRequest().fold(
          formWithErrors =>
            Future.successful(
              BadRequest(view(formWithErrors, waypoints, previousRegistrations))
            ),

          value => {
            val baseAnswers = request.userAnswers.getOrElse(UserAnswers(request.userId))

            for {
              updatedAnswers <- Future.fromTry(
                baseAnswers.set(
                  PreviousRegistrationIntermediaryNumberQuery,
                  value
                )
              )
              _ <- cc.sessionRepository.set(updatedAnswers)
            } yield Redirect(
              ViewOrChangePreviousRegistrationsMultiplePage
                .navigate(waypoints, baseAnswers, updatedAnswers)
                .route
            )
          }
        )
      }
    }

}

