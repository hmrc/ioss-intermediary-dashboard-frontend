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
import controllers.amend.routes
import forms.ViewOrChangePreviousRegistrationFormProvider
import models.amend.PreviousRegistration
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito
import org.mockito.Mockito.{times, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.ViewOrChangePreviousRegistrationPage
import pages.{EmptyWaypoints, Waypoints}
import play.api.data.Form
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.intermediaries.AccountService
import utils.FutureSyntax.FutureOps
import views.html.ViewOrChangePreviousRegistrationView

import java.time.LocalDate

class ViewOrChangePreviousRegistrationControllerSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  private val previousRegistration = PreviousRegistration(intermediaryNumber, LocalDate.now(), LocalDate.now().plusMonths(6))

  private val formProvider = new ViewOrChangePreviousRegistrationFormProvider()
  private val form: Form[Boolean] = formProvider(intermediaryNumber)

  private val waypoints: Waypoints = EmptyWaypoints

  private lazy val viewOrChangePreviousRegistrationRoute: String = routes.ViewOrChangePreviousRegistrationController.onPageLoad(waypoints).url
  private lazy val viewOrChangePreviousRegistrationSubmitRoute: String = routes.ViewOrChangePreviousRegistrationController.onSubmit(waypoints).url

  private val mockRegistrationConnector: RegistrationConnector = mock[RegistrationConnector]
  private val mockAccountService: AccountService = mock[AccountService]

  override def beforeEach(): Unit = {
    Mockito.reset(mockRegistrationConnector)
    Mockito.reset(mockAccountService)
  }

  "must return OK and the correct view for a GET when a single previous registration exists" in {

    when(mockAccountService.getPreviousRegistrations()(any())).thenReturn(Seq(previousRegistration).toFuture)

    val application = applicationBuilder(userAnswers = Some(emptyUserAnswersWithVatInfo))
      .overrides(bind[AccountService].toInstance(mockAccountService))
      .build()

    running(application) {
      val request = FakeRequest(GET, viewOrChangePreviousRegistrationRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[ViewOrChangePreviousRegistrationView]

      status(result) mustBe OK
      contentAsString(result) mustBe view(form, waypoints, intermediaryNumber)(request, messages(application)).toString
    }
  }
}