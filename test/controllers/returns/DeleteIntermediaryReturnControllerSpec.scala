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

import base.SpecBase
import forms.returns.DeleteIntermediaryReturnFormProvider
import models.Period
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito
import org.mockito.Mockito.{times, verify, verifyNoInteractions, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.YourAccountPage
import pages.saveForLater.ContinueSingleClientSavedReturnPage
import play.api.data.Form
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.SaveForLaterService
import utils.FutureSyntax.FutureOps
import views.html.returns.DeleteIntermediaryReturnView

class DeleteIntermediaryReturnControllerSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  private val mockSaveForLaterService: SaveForLaterService = mock[SaveForLaterService]
  private val mockSessionRepository: SessionRepository = mock[SessionRepository]

  private val formProvider = new DeleteIntermediaryReturnFormProvider()
  private val form: Form[Boolean] = formProvider()

  private val iossNumber: String = arbitraryIossNumber.arbitrary.sample.value
  private val period: Period = arbitraryPeriod.arbitrary.sample.value

  private lazy val deleteIntermediaryReturnRoute: String = routes.DeleteIntermediaryReturnController.onPageLoad(waypoints, iossNumber, period).url

  override def beforeEach(): Unit = {
    Mockito.reset(
      mockSaveForLaterService,
      mockSessionRepository
    )
  }

  "DeleteIntermediaryReturn Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, deleteIntermediaryReturnRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[DeleteIntermediaryReturnView]

        status(result) `mustBe` OK
        contentAsString(result) `mustBe` view(form, waypoints, iossNumber, period)(request, messages(application)).toString
        verifyNoInteractions(mockSaveForLaterService)
      }
    }

    "must redirect to the Your Account page when the user answers Yes" in {

      when(mockSaveForLaterService.deleteSavedUserAnswers(any(), any())(any())) thenReturn true.toFuture
      when(mockSessionRepository.clear(any())) thenReturn true.toFuture

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[SaveForLaterService].toInstance(mockSaveForLaterService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, deleteIntermediaryReturnRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) `mustBe` SEE_OTHER
        redirectLocation(result).value `mustBe` YourAccountPage.route(waypoints).url
        verify(mockSaveForLaterService, times(1)).deleteSavedUserAnswers(eqTo(iossNumber), eqTo(period))(any())
        verify(mockSessionRepository, times(1)).clear(eqTo(userAnswersId))
      }
    }

    "must redirect to the Continue Single Client Saved Return page when the user answers No" in {

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, deleteIntermediaryReturnRoute)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) `mustBe` SEE_OTHER
        redirectLocation(result).value `mustBe` ContinueSingleClientSavedReturnPage(iossNumber).route(waypoints).url
        verifyNoInteractions(mockSaveForLaterService)
        verifyNoInteractions(mockSessionRepository)
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, deleteIntermediaryReturnRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[DeleteIntermediaryReturnView]

        val result = route(application, request).value

        status(result) `mustBe` BAD_REQUEST
        contentAsString(result) `mustBe` view(boundForm, waypoints, iossNumber, period)(request, messages(application)).toString
        verifyNoInteractions(mockSaveForLaterService)
      }
    }
  }
}
