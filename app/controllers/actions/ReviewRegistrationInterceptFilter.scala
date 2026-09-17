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

package controllers.actions

import config.FrontendAppConfig
import models.UserAnswers
import models.requests.RegistrationRequest
import pages.{ReviewRegistrationInterceptPage, Waypoints}
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionFilter, Result}
import repositories.SessionRepository
import utils.FutureSyntax.FutureOps

import java.time.{Clock, LocalDate}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ReviewRegistrationInterceptFilter(
                                         clock: Clock,
                                         sessionRepository: SessionRepository,
                                         waypoints: Waypoints,
                                         frontendAppConfig: FrontendAppConfig
                                       )(implicit val executionContext: ExecutionContext) extends ActionFilter[RegistrationRequest] {

  override protected def filter[A](request: RegistrationRequest[A]): Future[Option[Result]] = {

    if (!frontendAppConfig.registrationReviewEnabled) {
      None.toFuture
    } else {
      val changeDate = request.registrationWrapper.etmpDisplayRegistration.adminUse.changeDate
      val needsReview = changeDate.exists { date =>
        !date.toLocalDate.plusYears(2).isAfter(LocalDate.now(clock))
      }

      if (!needsReview) {
        None.toFuture
      } else {
        sessionRepository.get(request.userId).flatMap {
          case Some(userAnswers) if userAnswers.get(ReviewRegistrationInterceptPage).contains(true) =>
            None.toFuture
          case Some(_) =>
            Some(Redirect(ReviewRegistrationInterceptPage.route(waypoints))).toFuture
          case None =>
            val userAnswers = UserAnswers(request.userId)

            sessionRepository.set(userAnswers).map { _ =>
              Some(Redirect(ReviewRegistrationInterceptPage.route(waypoints)))
            }
        }
      }
    }
  }
}

class ReviewRegistrationInterceptFilterProvider @Inject()(
                                                           clock: Clock,
                                                           sessionRepository: SessionRepository,
                                                           frontendAppConfig: FrontendAppConfig
                                                         )(implicit ec: ExecutionContext) {

  def apply(waypoints: Waypoints): ReviewRegistrationInterceptFilter = new ReviewRegistrationInterceptFilter(clock, sessionRepository, waypoints, frontendAppConfig)
}