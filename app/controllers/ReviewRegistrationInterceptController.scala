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

package controllers

import config.FrontendAppConfig
import controllers.actions.*

import javax.inject.Inject
import pages.{ReviewRegistrationInterceptPage, Waypoints}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ReviewRegistrationInterceptView

import scala.concurrent.{ExecutionContext, Future}

class ReviewRegistrationInterceptController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       identify: IdentifierAction,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       frontendAppConfig: FrontendAppConfig,
                                       sessionRepository: SessionRepository,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: ReviewRegistrationInterceptView
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val changeRegistrationUrl = frontendAppConfig.changeYourRegistrationUrl
      Ok(view(waypoints, changeRegistrationUrl))
  }
  
  def onSubmit(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      for {
        updatedAnswers <- Future.fromTry(request.userAnswers.set(ReviewRegistrationInterceptPage, true))
        _ <- sessionRepository.set(updatedAnswers)
      } yield Redirect(controllers.returns.routes.ClientsOutstandingReturnsListController.onPageLoad(waypoints))
      
  }
}
