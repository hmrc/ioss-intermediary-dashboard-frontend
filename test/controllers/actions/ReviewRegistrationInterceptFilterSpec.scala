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

import base.SpecBase
import config.FrontendAppConfig
import models.UserAnswers
import models.etmp.{EtmpAdminUse, EtmpDisplayRegistration, EtmpExclusion, EtmpOtherAddress}
import models.requests.RegistrationRequest
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, times, verify, verifyNoInteractions, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.{ReviewRegistrationInterceptPage, Waypoints}
import play.api.mvc.Result
import play.api.mvc.Results.Redirect
import play.api.test.FakeRequest
import play.api.test.Helpers.running
import repositories.SessionRepository

import java.time.{Clock, LocalDateTime}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ReviewRegistrationInterceptFilterSpec extends SpecBase with MockitoSugar {

  class Harness(
                 clock: Clock,
                 sessionRepository: SessionRepository,
                 waypoints: Waypoints,
                 frontendAppConfig: FrontendAppConfig
               ) extends ReviewRegistrationInterceptFilter(clock, sessionRepository, waypoints, frontendAppConfig) {

    def callFilter(request: RegistrationRequest[_]): Future[Option[Result]] = filter(request)
  }

  private val registrationNotNeedingReview: EtmpDisplayRegistration =
    arbitraryEtmpDisplayRegistration.arbitrary.sample.value.copy(
      adminUse = EtmpAdminUse(Some(LocalDateTime.now(stubClockAtArbitraryDate).minusYears(1)))
    )

  private val registrationNeedingReview: EtmpDisplayRegistration =
    registrationNotNeedingReview.copy(
      adminUse = EtmpAdminUse(
        Some(LocalDateTime.now(stubClockAtArbitraryDate).minusYears(3))
      )
    )

  ".filter" - {

    "when registrationReviewEnabled is false" - {

      "must return None even when the registration needs reviewing" in {

        val sessionRepository = mock[SessionRepository]

        val application = applicationBuilder()
          .configure("features.registration-review-enabled" -> false)
          .build()

        running(application) {

          val request = RegistrationRequest(
            FakeRequest(),
            userId = userAnswersId,
            enrolments = enrolments,
            vrn = vrn,
            intermediaryNumber = intermediaryNumber,
            registrationWrapper = registrationWrapper.copy(
              etmpDisplayRegistration = registrationNeedingReview
            )
          )

          val frontendAppConfig = application.injector.instanceOf[FrontendAppConfig]
          val controller = new Harness(stubClockAtArbitraryDate, sessionRepository, waypoints, frontendAppConfig)

          val result = controller.callFilter(request).futureValue

          result mustBe None

          verifyNoInteractions(sessionRepository)
        }
      }
    }

    "when registrationReviewEnabled is true" - {

      "must return None when the registration was updated within two years" in {

        val sessionRepository = mock[SessionRepository]

        val application = applicationBuilder()
          .configure("features.registration-review-enabled" -> true)
          .build()

        running(application) {

          val request = RegistrationRequest(
            FakeRequest(),
            userId = userAnswersId,
            enrolments = enrolments,
            vrn = vrn,
            intermediaryNumber = intermediaryNumber,
            registrationWrapper = registrationWrapper.copy(
              etmpDisplayRegistration = registrationNotNeedingReview
            )
          )

          val frontendAppConfig = application.injector.instanceOf[FrontendAppConfig]
          val controller = new Harness(stubClockAtArbitraryDate, sessionRepository, waypoints, frontendAppConfig)

          val result = controller.callFilter(request).futureValue

          result mustBe None

          verifyNoInteractions(sessionRepository)
        }
      }

      "must return None when the registration needs reviewing but the user has already skipped the intercept" in {

        val skippedAnswers = emptyUserAnswers.set(ReviewRegistrationInterceptPage, true).success.value
        val sessionRepository = mock[SessionRepository]

        when(sessionRepository.get(userAnswersId)) thenReturn Future.successful(Some(skippedAnswers))

        val application = applicationBuilder()
          .configure("features.registration-review-enabled" -> true)
          .build()

        running(application) {

          val request = RegistrationRequest(
            FakeRequest(),
            userId = userAnswersId,
            enrolments = enrolments,
            vrn = vrn,
            intermediaryNumber = intermediaryNumber,
            registrationWrapper = registrationWrapper.copy(
              etmpDisplayRegistration = registrationNeedingReview
            )
          )

          val frontendAppConfig = application.injector.instanceOf[FrontendAppConfig]
          val controller = new Harness(stubClockAtArbitraryDate, sessionRepository, waypoints, frontendAppConfig)

          val result = controller.callFilter(request).futureValue

          result mustBe None

          verify(sessionRepository, times(1)).get(userAnswersId)

          verify(sessionRepository, never()).set(any[UserAnswers])
        }
      }

      "must redirect to ReviewRegistrationInterceptPage when the registration needs reviewing and the user has not skipped the intercept" in {

        val sessionRepository = mock[SessionRepository]

        when(sessionRepository.get(userAnswersId)) thenReturn Future.successful(Some(emptyUserAnswers))

        val application = applicationBuilder()
          .configure("features.registration-review-enabled" -> true)
          .build()

        running(application) {

          val request = RegistrationRequest(
            FakeRequest(),
            userId = userAnswersId,
            enrolments = enrolments,
            vrn = vrn,
            intermediaryNumber = intermediaryNumber,
            registrationWrapper = registrationWrapper.copy(
              etmpDisplayRegistration = registrationNeedingReview
            )
          )

          val frontendAppConfig = application.injector.instanceOf[FrontendAppConfig]
          val controller = new Harness(stubClockAtArbitraryDate, sessionRepository, waypoints, frontendAppConfig)

          val result = controller.callFilter(request).futureValue

          result mustBe Some(Redirect(ReviewRegistrationInterceptPage.route(waypoints)))

          verify(sessionRepository, times(1)).get(userAnswersId)

          verify(sessionRepository, never()).set(any[UserAnswers])
        }
      }

      "must create UserAnswers and redirect to ReviewRegistrationInterceptPage when the registration needs reviewing and no UserAnswers exist" in {

        val sessionRepository = mock[SessionRepository]

        when(sessionRepository.get(userAnswersId)) thenReturn Future.successful(None)

        when(sessionRepository.set(any[UserAnswers])) thenReturn Future.successful(true)

        val application = applicationBuilder()
          .configure("features.registration-review-enabled" -> true)
          .build()

        running(application) {

          val request = RegistrationRequest(
            FakeRequest(),
            userId = userAnswersId,
            enrolments = enrolments,
            vrn = vrn,
            intermediaryNumber = intermediaryNumber,
            registrationWrapper = registrationWrapper.copy(
              etmpDisplayRegistration = registrationNeedingReview
            )
          )

          val frontendAppConfig = application.injector.instanceOf[FrontendAppConfig]
          val controller = new Harness(stubClockAtArbitraryDate, sessionRepository, waypoints, frontendAppConfig)

          val result = controller.callFilter(request).futureValue

          result mustBe Some(Redirect(ReviewRegistrationInterceptPage.route(waypoints)))

          verify(sessionRepository, times(1)).get(userAnswersId)

          verify(sessionRepository, times(1)).set(any[UserAnswers])
        }
      }
    }
  }
}
