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

package controllers.saveForLater

import connectors.RegistrationConnector
import controllers.actions.*
import forms.saveForLater.SelectClientSavedReturnFormProvider
import models.UserAnswers
import models.etmp.EtmpClientDetails
import pages.Waypoints
import pages.saveForLater.SelectClientSavedReturnPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SaveForLaterService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.FutureSyntax.FutureOps
import views.html.saveForLater.SelectClientSavedReturnView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SelectClientSavedReturnController @Inject()(
                                                   override val messagesApi: MessagesApi,
                                                   cc: AuthenticatedControllerComponents,
                                                   formProvider: SelectClientSavedReturnFormProvider,
                                                   saveForLaterService: SaveForLaterService,
                                                   registrationConnector: RegistrationConnector,
                                                   view: SelectClientSavedReturnView
                                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad(waypoints: Waypoints): Action[AnyContent] = (cc.identify andThen cc.getData).async {
    implicit request =>

      val userAnswers = request.userAnswers match {
        case Some(answers) => answers
        case _ => UserAnswers(request.userId)
      }

      saveForLaterService.getAllClientSavedAnswers().flatMap { allSavedUserAnswers =>
        registrationConnector.getRegistration(request.intermediaryNumber).map { registrationWrapper =>

          val allClientsDetailsWithSavedAnswers: Seq[EtmpClientDetails] = allSavedUserAnswers.flatMap { savedAnswers =>
            registrationWrapper.etmpDisplayRegistration.clientDetails.filter(_.clientIossID == savedAnswers.iossNumber)
          }

          val form: Form[EtmpClientDetails] = formProvider(allClientsDetailsWithSavedAnswers)

          val preparedForm = userAnswers.get(SelectClientSavedReturnPage) match {
            case None => form
            case Some(value) => form.fill(value)
          }

          Ok(view(preparedForm, waypoints, allClientsDetailsWithSavedAnswers))
        }
      }
  }

  def onSubmit(waypoints: Waypoints): Action[AnyContent] = (cc.identify andThen cc.getData).async {
    implicit request =>

      val userAnswers = request.userAnswers match {
        case Some(answers) => answers
        case _ => UserAnswers(request.userId)
      }

      saveForLaterService.getAllClientSavedAnswers().flatMap { allSavedUserAnswers =>
        registrationConnector.getRegistration(request.intermediaryNumber).flatMap { registrationWrapper =>

          val allClientsDetailsWithSavedAnswers: Seq[EtmpClientDetails] = allSavedUserAnswers.flatMap { savedAnswers =>
            registrationWrapper.etmpDisplayRegistration.clientDetails.filter(_.clientIossID == savedAnswers.iossNumber)
          }

          val form: Form[EtmpClientDetails] = formProvider(allClientsDetailsWithSavedAnswers)

          form.bindFromRequest().fold(
            formWithErrors =>
              BadRequest(view(formWithErrors, waypoints, allClientsDetailsWithSavedAnswers)).toFuture,

            value =>
              for {
                updatedAnswers <- Future.fromTry(userAnswers.set(SelectClientSavedReturnPage, value))
                _ <- cc.sessionRepository.set(updatedAnswers)
              } yield Redirect(SelectClientSavedReturnPage.navigate(waypoints, userAnswers, updatedAnswers).route)
          )
        }
      }
  }
}
