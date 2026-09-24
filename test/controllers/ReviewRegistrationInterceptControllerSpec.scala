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

import base.SpecBase
import config.FrontendAppConfig
import pages.ReviewRegistrationInterceptPage
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.ReviewRegistrationInterceptView

class ReviewRegistrationInterceptControllerSpec extends SpecBase {

  "ReviewRegistrationIntercept Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.ReviewRegistrationInterceptController.onPageLoad().url)
        val frontendAppConfig = application.injector.instanceOf[FrontendAppConfig]
        val result = route(application, request).value

        val view = application.injector.instanceOf[ReviewRegistrationInterceptView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(waypoints, frontendAppConfig.changeYourRegistrationUrl)(request, messages(application)).toString
      }
    }

    "must set ReviewRegistrationInterceptPage to true and redirect to outstanding returns on submit" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(POST, routes.ReviewRegistrationInterceptController.onSubmit(waypoints).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.returns.routes.ClientsOutstandingReturnsListController.onPageLoad(waypoints).url

        val repository = application.injector.instanceOf[SessionRepository]

        val updatedAnswers = repository.get(emptyUserAnswers.id).futureValue.value

        updatedAnswers.get(ReviewRegistrationInterceptPage).value mustEqual true
      }
    }
  }
}
