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
import forms.ViewOrChangePreviousRegistrationFormProvider
import logging.Logging
import pages.{ViewOrChangePreviousRegistrationPage, Waypoints, YourAccountPage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import queries.PreviousRegistrationIntermediaryNumberQuery
import services.intermediaries.AccountService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.FutureSyntax.FutureOps
import views.html.ViewOrChangePreviousRegistrationView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ViewOrChangePreviousRegistrationController @Inject()(
                                                            override val messagesApi: MessagesApi,
                                                            cc: AuthenticatedControllerComponents,
                                                            formProvider: ViewOrChangePreviousRegistrationFormProvider,
                                                            accountService: AccountService,
                                                            view: ViewOrChangePreviousRegistrationView
                                                          )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging {

  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad(waypoints: Waypoints): Action[AnyContent] =
    cc.identifyAndGetRegistration.async {
      implicit request =>
        accountService.getPreviousRegistrations().flatMap { previousRegistrations =>

          previousRegistrations.size match {
            case 0 =>
              val exception = new IllegalStateException("Must have one or more previous registrations")
              logger.error(exception.getMessage, exception)
              throw exception
            case 1 =>
              val intermediaryNumber: String = previousRegistrations.map(_.intermediaryNumber).head

              val form: Form[Boolean] = formProvider(intermediaryNumber)

              Ok(view(form, waypoints, intermediaryNumber)).toFuture
            case _ =>
              Redirect(YourAccountPage.route(waypoints).url).toFuture
          }
        }
    }


  def onSubmit(waypoints: Waypoints): Action[AnyContent] =
    cc.identifyAndGetData.async {
      implicit request =>

        accountService.getPreviousRegistrations().flatMap { previousRegistrations =>

          val intermediaryNumber: String = previousRegistrations.map(_.intermediaryNumber).head

          val form: Form[Boolean] = formProvider(intermediaryNumber)

          form.bindFromRequest().fold(
            formWithErrors =>
              BadRequest(view(formWithErrors, waypoints, intermediaryNumber)).toFuture,

            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(ViewOrChangePreviousRegistrationPage, value))
                updateAnswersWithIntermediaryNumber <- Future.fromTry(updatedAnswers.set(PreviousRegistrationIntermediaryNumberQuery, intermediaryNumber))
                _ <- cc.sessionRepository.set(updateAnswersWithIntermediaryNumber)

              } yield Redirect(ViewOrChangePreviousRegistrationPage.navigate(waypoints, request.userAnswers, updateAnswersWithIntermediaryNumber).route)
          )
        }
    }
}

