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

package controllers.saveForLater

import config.FrontendAppConfig
import connectors.RegistrationConnector
import controllers.actions.*
import forms.saveForLater.ContinueSingleClientSavedReturnFormProvider
import models.saveForLater.ContinueSingleClientSavedReturn
import models.saveForLater.ContinueSingleClientSavedReturn.{No, Yes}
import models.{Period, UserAnswers}
import pages.Waypoints
import pages.returns.DeleteIntermediaryReturnPage
import pages.saveForLater.ContinueSingleClientSavedReturnPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SaveForLaterService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.FutureSyntax.FutureOps
import views.html.saveForLater.ContinueSingleClientSavedReturnView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ContinueSingleClientSavedReturnController @Inject()(
                                                           override val messagesApi: MessagesApi,
                                                           cc: AuthenticatedControllerComponents,
                                                           formProvider: ContinueSingleClientSavedReturnFormProvider,
                                                           saveForLaterService: SaveForLaterService,
                                                           registrationConnector: RegistrationConnector,
                                                           frontendAppConfig: FrontendAppConfig,
                                                           view: ContinueSingleClientSavedReturnView
                                                         )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad(waypoints: Waypoints, iossNumber: String): Action[AnyContent] = (cc.identify andThen cc.getData).async {
    implicit request =>

      val userAnswers = request.userAnswers match {
        case Some(answers) => answers
        case _ => UserAnswers(request.userId)
      }

      registrationConnector.getRegistration(request.intermediaryNumber).flatMap { registration =>
        saveForLaterService.getAllSavedReturnsForClient(iossNumber).flatMap { allSavedUserAnswersForClient =>

          val clientName: String = registration.etmpDisplayRegistration.clientDetails
            .filter(_.clientIossID == iossNumber)
            .map(_.clientName).headOption
            .getOrElse(throw Exception("Failed to match client to Intermediary"))

          val form: Form[ContinueSingleClientSavedReturn] = formProvider(
            clientName,
            iossNumber
          )
          val preparedForm = userAnswers.get(ContinueSingleClientSavedReturnPage(iossNumber)) match {
            case None => form
            case Some(value) => form.fill(value)
          }

          Ok(view(preparedForm, waypoints, clientName, iossNumber)).toFuture
        }
      }
  }

  def onSubmit(waypoints: Waypoints, iossNumber: String): Action[AnyContent] = (cc.identify andThen cc.getData).async {
    implicit request =>

      val userAnswers = request.userAnswers match {
        case Some(answers) => answers
        case _ => UserAnswers(request.userId)
      }

      registrationConnector.getRegistration(request.intermediaryNumber).flatMap { registration =>
        saveForLaterService.getAllSavedReturnsForClient(iossNumber).flatMap { allSavedUserAnswersForClient =>

          val clientName: String = registration.etmpDisplayRegistration.clientDetails
            .filter(_.clientIossID == iossNumber)
            .map(_.clientName).headOption
            .getOrElse(throw Exception("Failed to match client to Intermediary"))

          val period: Period = allSavedUserAnswersForClient.head.period

          val form: Form[ContinueSingleClientSavedReturn] = formProvider(
            clientName, iossNumber
          )

          form.bindFromRequest().fold(
            formWithErrors =>
              BadRequest(view(formWithErrors, waypoints, clientName, iossNumber)).toFuture,
            {
              case value@Yes =>
                val redirectUrl: String = s"${frontendAppConfig.startIntermediarySavedReturns}/$iossNumber/$period"

                for {
                  updatedAnswers <- Future.fromTry(userAnswers.set(ContinueSingleClientSavedReturnPage(iossNumber), value))
                  _ <- cc.sessionRepository.set(updatedAnswers)
                } yield Redirect(redirectUrl)

              case value@No =>
                Redirect(DeleteIntermediaryReturnPage(iossNumber, period).route(waypoints)).toFuture
            }
          )
        }
      }
  }
}
