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

package controllers.returns

import controllers.actions.*
import forms.returns.DeleteIntermediaryReturnFormProvider
import models.Period
import pages.saveForLater.ContinueSingleClientSavedReturnPage
import pages.{Waypoints, YourAccountPage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SaveForLaterService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.FutureSyntax.FutureOps
import views.html.returns.DeleteIntermediaryReturnView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class DeleteIntermediaryReturnController @Inject()(
                                                    override val messagesApi: MessagesApi,
                                                    cc: AuthenticatedControllerComponents,
                                                    formProvider: DeleteIntermediaryReturnFormProvider,
                                                    saveForLaterService: SaveForLaterService,
                                                    view: DeleteIntermediaryReturnView
                                                  )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  protected val controllerComponents: MessagesControllerComponents = cc

  val form: Form[Boolean] = formProvider()

  def onPageLoad(waypoints: Waypoints, iossNumber: String, period: Period): Action[AnyContent] = (cc.actionBuilder andThen cc.identify andThen cc.getData) {
    implicit request =>

      Ok(view(form, waypoints, iossNumber, period))
  }

  def onSubmit(waypoints: Waypoints, iossNumber: String, period: Period): Action[AnyContent] = (cc.actionBuilder andThen cc.identify andThen cc.getData).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          BadRequest(view(formWithErrors, waypoints, iossNumber, period)).toFuture,

        value =>
          if (value) {
            for {
              _ <- cc.sessionRepository.clear(request.userId)
              _ <- saveForLaterService.deleteSavedUserAnswers(iossNumber, period)
            } yield Redirect(YourAccountPage.route(waypoints).url)
          } else {
            Redirect(ContinueSingleClientSavedReturnPage(iossNumber).route(waypoints).url).toFuture
          }
      )
  }
}
