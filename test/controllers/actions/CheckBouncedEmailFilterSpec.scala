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

package controllers.actions

import base.SpecBase
import models.requests.RegistrationRequest
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.Result
import play.api.mvc.Results.Redirect
import play.api.test.FakeRequest
import play.api.test.Helpers.running

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

  class CheckBouncedEmailFilterSpec extends SpecBase with MockitoSugar {

  class Harness extends CheckBouncedEmailFilterImpl {
    def callFilter(request: RegistrationRequest[_]): Future[Option[Result]] = filter(request)
  }
  
  ".filter" - {
    
    "must return None when the unusableStatus is False" in {

      val updatedRegistrationWrapper = registrationWrapper.copy(
        etmpDisplayRegistration = arbitraryEtmpDisplayRegistration.arbitrary.sample.value.copy(
          schemeDetails = arbitraryEtmpDisplaySchemeDetails.arbitrary.sample.value.copy(
            unusableStatus = false
          )
        )
      )
      
      val application = applicationBuilder(None).build()
      
      running(application) {
        
        val request = RegistrationRequest(
          request = FakeRequest(),
          userId = userAnswersId,
          enrolments = testEnrolments,
          vrn = vrn,
          intermediaryNumber = intermediaryNumber,
          registrationWrapper = updatedRegistrationWrapper
        )
        
        val controller = new Harness
        
        val result = controller.callFilter(request).futureValue
        
        result mustBe None
      }
    }

    "must redirect to InterceptUnusableEmail page when the unusableStatus is True" in {

      val updatedRegistrationWrapper = registrationWrapper.copy(
        etmpDisplayRegistration = arbitraryEtmpDisplayRegistration.arbitrary.sample.value.copy(
          schemeDetails = arbitraryEtmpDisplaySchemeDetails.arbitrary.sample.value.copy(
            unusableStatus = true
          )
        )
      )

      val application = applicationBuilder(None).build()

      running(application) {

        val request = RegistrationRequest(
          request = FakeRequest(),
          userId = userAnswersId,
          enrolments = testEnrolments,
          vrn = vrn,
          intermediaryNumber = intermediaryNumber,
          registrationWrapper = updatedRegistrationWrapper
        )

        val controller = new Harness

        val result = controller.callFilter(request).futureValue

        result.value mustBe Redirect(controllers.routes.InterceptUnusableEmailController.onPageLoad().url)
      }
    }
  }
}
