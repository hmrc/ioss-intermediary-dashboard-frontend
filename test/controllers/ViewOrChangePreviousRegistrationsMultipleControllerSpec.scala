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
import forms.ViewOrChangePreviousRegistrationsMultipleFormProvider
import models.amend.PreviousRegistration
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.when
import org.scalacheck.Gen
import org.scalatestplus.mockito.MockitoSugar
import pages.{EmptyWaypoints, Waypoints}
import play.api.data.Form
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.intermediaries.AccountService
import utils.FutureSyntax.FutureOps
import views.html.ViewOrChangePreviousRegistrationsMultipleView

class ViewOrChangePreviousRegistrationsMultipleControllerSpec extends SpecBase with MockitoSugar {
  private lazy val viewOrChangePreviousRegistrationsMultipleRoute = amend.routes.ViewOrChangePreviousRegistrationsMultipleController.onPageLoad(waypoints).url
  private val waypoints: Waypoints = EmptyWaypoints
  private val previousRegistrations: Seq[PreviousRegistration] = Gen.listOfN(4, arbitraryPreviousRegistration.arbitrary).sample.value
  private val formProvider = new ViewOrChangePreviousRegistrationsMultipleFormProvider()
  private val form: Form[String] = formProvider(previousRegistrations)


  private val mockAccountService: AccountService = mock[AccountService]


  "ViewOrChangePreviousRegistrationsMultiple Controller" - {

    "must return OK and the correct view for a GET" in {

      when(mockAccountService.getPreviousRegistrations()(any())) thenReturn previousRegistrations.toFuture

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersWithVatInfo))
        .overrides(bind[AccountService].toInstance(mockAccountService))
        .build()

      running(application) {
        val request = FakeRequest(GET, viewOrChangePreviousRegistrationsMultipleRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ViewOrChangePreviousRegistrationsMultipleView]

        status(result) mustBe OK
        contentAsString(result) mustBe view(form, waypoints, previousRegistrations)(request, messages(application)).toString
      }
    }

  }
}
