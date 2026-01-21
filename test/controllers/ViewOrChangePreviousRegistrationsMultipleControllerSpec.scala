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

package controllers

import base.SpecBase
import connectors.RegistrationConnector
import forms.amend.ViewOrChangePreviousRegistrationsMultipleFormProvider
import models.UserAnswers
import models.amend.PreviousRegistration
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito
import org.mockito.Mockito.{times, verify, when}
import org.scalacheck.Gen
import org.scalatestplus.mockito.MockitoSugar
import pages.amend.ViewOrChangePreviousRegistrationsMultiplePage
import pages.{EmptyWaypoints, JourneyRecoveryPage, Waypoints}
import play.api.data.Form
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import queries.PreviousRegistrationIntermediaryNumberQuery
import repositories.SessionRepository
import services.intermediaries.AccountService
import utils.FutureSyntax.FutureOps
import views.html.ViewOrChangePreviousRegistrationsMultipleView

class ViewOrChangePreviousRegistrationsMultipleControllerSpec extends SpecBase with MockitoSugar {
  private lazy val viewOrChangePreviousRegistrationsMultipleRoute = amend.routes.ViewOrChangePreviousRegistrationsMultipleController.onPageLoad(waypoints).url
  private val waypoints: Waypoints = EmptyWaypoints
  private val previousRegistrations: Seq[PreviousRegistration] = Gen.listOfN(4, arbitraryPreviousRegistration.arbitrary).sample.value
  private val formProvider = new ViewOrChangePreviousRegistrationsMultipleFormProvider()
  private val form: Form[String] = formProvider(previousRegistrations)
  private val intermediaryNumber: String = previousRegistrations.map(_.intermediaryNumber).head
  private val invalidIntermediaryNumber: String = arbitraryPreviousRegistration.arbitrary
    .suchThat(_.intermediaryNumber.toSeq != previousRegistrations.map(_.intermediaryNumber)).sample.value.intermediaryNumber


  private val mockAccountService: AccountService = mock[AccountService]
  private val mockRegistrationConnector = mock[RegistrationConnector]



  "ViewOrChangePreviousRegistrationsMultiple Controller" - {

    "must return OK and the correct view for a GET" in {

      when(mockRegistrationConnector.getRegistration(any())(any())).thenReturn(registrationWrapper.toFuture)

      when(mockRegistrationConnector.displayRegistration(any())(any())).thenReturn(Right(registrationWrapper).toFuture)

      when(mockAccountService.getPreviousRegistrations()(any())).thenReturn(previousRegistrations.toFuture)

      val application = applicationBuilder(
        userAnswers = Some(emptyUserAnswersWithVatInfo)
      )
        .overrides(
          bind[RegistrationConnector].toInstance(mockRegistrationConnector),
          bind[AccountService].toInstance(mockAccountService)
        ).build()

      running(application) {
        val request = FakeRequest(GET, viewOrChangePreviousRegistrationsMultipleRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ViewOrChangePreviousRegistrationsMultipleView]

        val expectedForm: Form[String] = formProvider(previousRegistrations)

        status(result) mustBe OK
        contentAsString(result) mustBe
          view(
            expectedForm,
            waypoints,
            previousRegistrations
          )(request, messages(application)).toString
      }
    }

    "must return OK and the correct view when there are userAnswers" in {

      when(mockRegistrationConnector.getRegistration(any())(any())).thenReturn(registrationWrapper.toFuture)

      when(mockRegistrationConnector.displayRegistration(any())(any())).thenReturn(Right(registrationWrapper).toFuture)

      when(mockAccountService.getPreviousRegistrations()(any())).thenReturn(previousRegistrations.toFuture)

      val userAnswers =
        emptyUserAnswersWithVatInfo
          .set(PreviousRegistrationIntermediaryNumberQuery, intermediaryNumber)
          .success
          .value

      val application = applicationBuilder(
        userAnswers = Some(userAnswers)
      )
        .overrides(
          bind[RegistrationConnector].toInstance(mockRegistrationConnector),
          bind[AccountService].toInstance(mockAccountService)
        ).build()

      running(application) {
        val request = FakeRequest(GET, viewOrChangePreviousRegistrationsMultipleRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ViewOrChangePreviousRegistrationsMultipleView]

        val expectedForm: Form[String] = formProvider(previousRegistrations)

        status(result) mustBe OK
        contentAsString(result) mustBe
          view(
            expectedForm,
            waypoints,
            previousRegistrations
          )(request, messages(application)).toString
      }
    }

    "must save the answer and redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn true.toFuture
      when(mockAccountService.getPreviousRegistrations()(any())) thenReturn previousRegistrations.toFuture

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersWithVatInfo))
        .overrides(bind[AccountService].toInstance(mockAccountService))
        .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
        .build()

      running(application) {
        val request =
          FakeRequest(POST, viewOrChangePreviousRegistrationsMultipleRoute)
            .withFormUrlEncodedBody(("value", intermediaryNumber))

        val result = route(application, request).value
        val expectedAnswers: UserAnswers = emptyUserAnswersWithVatInfo
          .set(PreviousRegistrationIntermediaryNumberQuery, intermediaryNumber).success.value


        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe
          ViewOrChangePreviousRegistrationsMultiplePage.navigate(waypoints, emptyUserAnswersWithVatInfo, expectedAnswers).url
        verify(mockSessionRepository, times(1)).set(eqTo(expectedAnswers))
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      when(mockAccountService.getPreviousRegistrations()(any())) thenReturn previousRegistrations.toFuture

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersWithVatInfo))
        .overrides(bind[AccountService].toInstance(mockAccountService))
        .build()

      running(application) {
        val request =
          FakeRequest(POST, viewOrChangePreviousRegistrationsMultipleRoute)
            .withFormUrlEncodedBody(("value", invalidIntermediaryNumber))

        val boundForm = form.bind(Map("value" -> invalidIntermediaryNumber))

        val view = application.injector.instanceOf[ViewOrChangePreviousRegistrationsMultipleView]

        val result = route(application, request).value

        status(result) mustBe BAD_REQUEST
        contentAsString(result) mustBe view(boundForm, waypoints, previousRegistrations)(request, messages(application)).toString
      }
    }

  }
}
