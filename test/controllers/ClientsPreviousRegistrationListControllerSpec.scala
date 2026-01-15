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
import forms.ClientsPreviousRegistrationListFormProvider
import models.returns.PreviousRegistration
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.data.Form
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SelectedPreviousRegistrationRepository
import services.ioss.AccountService
import testutils.PreviousRegistrationData.{previousRegistrations, selectedPreviousRegistration}
import utils.FutureSyntax.FutureOps
import views.html.ClientsPreviousRegistrationListView


class ClientsPreviousRegistrationListControllerSpec extends SpecBase with MockitoSugar {

  private val form: Form[PreviousRegistration] = new ClientsPreviousRegistrationListFormProvider()(previousRegistrations)

  private val mockAccountService = mock[AccountService]
  private val mockSelectedPreviousRegistrationRepository = mock[SelectedPreviousRegistrationRepository]

  "ClientsPreviousRegistrationList Controller" - {

    "must return OK and the correct view for a GET" - {

      "when there are multiple previous registrations and the form is empty" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[AccountService].toInstance(mockAccountService))
          .build()

        running(application) {

          when(mockAccountService.getPreviousRegistrations()(any())) thenReturn previousRegistrations.toFuture

          val request = FakeRequest(GET, routes.ClientsPreviousRegistrationListController.onPageLoad(waypoints).url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[ClientsPreviousRegistrationListView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form, waypoints, previousRegistrations)(request, messages(application)).toString
        }
      }

      "when there are multiple previous registrations and the form is prefilled" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[AccountService].toInstance(mockAccountService))
          .overrides(bind[SelectedPreviousRegistrationRepository].toInstance(mockSelectedPreviousRegistrationRepository))
          .build()

        running(application) {

          when(mockAccountService.getPreviousRegistrations()(any())) thenReturn previousRegistrations.toFuture
          when(mockSelectedPreviousRegistrationRepository.get(userAnswersId)) thenReturn Some(selectedPreviousRegistration).toFuture

          val request = FakeRequest(GET, routes.ClientsPreviousRegistrationListController.onPageLoad(waypoints).url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[ClientsPreviousRegistrationListView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual
            view(form.fill(selectedPreviousRegistration.previousRegistration), waypoints, previousRegistrations)(request, messages(application)).toString
        }
      }
    }

    "must redirect to JourneyRecoveryPage for a GET when there are no previous registrations" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[AccountService].toInstance(mockAccountService))
        .build()

      running(application) {

        when(mockAccountService.getPreviousRegistrations()(any())) thenReturn Seq.empty.toFuture

        val request = FakeRequest(GET, routes.ClientsPreviousRegistrationListController.onPageLoad(waypoints).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to ClientsPreviousRegistrationReturnsListPage when valid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[AccountService].toInstance(mockAccountService))
        .overrides(bind[SelectedPreviousRegistrationRepository].toInstance(mockSelectedPreviousRegistrationRepository))
        .build()

      running(application) {

        when(mockAccountService.getPreviousRegistrations()(any())) thenReturn previousRegistrations.toFuture
        when(mockSelectedPreviousRegistrationRepository.set(any())) thenReturn selectedPreviousRegistration.toFuture

        val request = FakeRequest(POST, routes.ClientsPreviousRegistrationListController.onPageLoad(waypoints).url)
          .withFormUrlEncodedBody(("value", selectedPreviousRegistration.previousRegistration.intermediaryNumber))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ClientsPreviousRegistrationReturnsListController.onPageLoad(waypoints).url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[AccountService].toInstance(mockAccountService))
        .build()

      running(application) {

        when(mockAccountService.getPreviousRegistrations()(any())) thenReturn previousRegistrations.toFuture

        val request = FakeRequest(POST, routes.ClientsPreviousRegistrationListController.onPageLoad(waypoints).url)
          .withFormUrlEncodedBody(("value", ""))

        val result = route(application, request).value

        val view = application.injector.instanceOf[ClientsPreviousRegistrationListView]

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual
          view(form.bind(Map("value" -> "")), waypoints, previousRegistrations)(request, messages(application)).toString
      }
    }
  }
}
