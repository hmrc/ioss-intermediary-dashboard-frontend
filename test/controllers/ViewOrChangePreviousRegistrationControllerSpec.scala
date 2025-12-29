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
import config.FrontendAppConfig
import connectors.RegistrationConnector
import controllers.amend.routes
import models.amend.PreviousRegistration
import models.responses.InternalServerError
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.{EmptyWaypoints, Waypoints}
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.intermediaries.AccountService
import utils.FutureSyntax.FutureOps
import viewmodels.amend.ViewOrChangePreviousRegistrationViewModel
import views.html.ViewOrChangePreviousRegistrationView

import java.time.LocalDate

class ViewOrChangePreviousRegistrationControllerSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  private lazy val viewOrChangePreviousRegistrationRoute: String = routes.ViewOrChangePreviousRegistrationController.onPageLoad(waypoints).url
  private val previousRegistration = PreviousRegistration(intermediaryNumber, LocalDate.now(), LocalDate.now().plusMonths(6))
  private val clientDetails = registrationWrapper.etmpDisplayRegistration.clientDetails
  private val waypoints: Waypoints = EmptyWaypoints
  private val mockRegistrationConnector: RegistrationConnector = mock[RegistrationConnector]
  private val mockAccountService: AccountService = mock[AccountService]

  override def beforeEach(): Unit = {
    Mockito.reset(mockRegistrationConnector)
    Mockito.reset(mockAccountService)
  }

  "must return OK and the correct view for a GET when a single previous registration exists" in {

    when(mockAccountService.getPreviousRegistrations()(any())).thenReturn(Seq(previousRegistration).toFuture)
    when(mockAccountService.getRegistrationClientDetails(any())(any())).thenReturn(Right(clientDetails).toFuture)

    val application = applicationBuilder(userAnswers = Some(emptyUserAnswersWithVatInfo))
      .overrides(bind[AccountService].toInstance(mockAccountService))
      .build()

    val appConfig = application.injector.instanceOf[FrontendAppConfig]

    val returnToCurrentRegUrl: String = appConfig.viewClientsListUrl
    val changeRegistrationRedirectUrl: String = "ADD-CHANGE-REG-LINK-HERE" // TODO: Update link


    running(application) {
      implicit val msgs: Messages = messages(application)

      val request = FakeRequest(GET, viewOrChangePreviousRegistrationRoute)

      val viewModel = ViewOrChangePreviousRegistrationViewModel(
        clientDetails,
        changeRegistrationRedirectUrl
      )

      val result = route(application, request).value

      val view = application.injector.instanceOf[ViewOrChangePreviousRegistrationView]

      status(result) mustBe OK
      contentAsString(result) mustBe view(
        waypoints,
        intermediaryNumber,
        viewModel,
        returnToCurrentRegUrl
      )(request, messages(application)).toString
    }
  }

  "must return IllegalStateException if details are not retrieved" in {

    val mockAccountService = mock[AccountService]
    when(mockAccountService.getPreviousRegistrations()(any())).thenReturn(Seq.empty.toFuture)

    val application = applicationBuilder(userAnswers = Some(emptyUserAnswersWithVatInfo))
      .overrides(bind[AccountService].toInstance(mockAccountService))
      .build()

    running(application) {

      val request = FakeRequest(GET, viewOrChangePreviousRegistrationRoute)

      val result = route(application, request).value.failed

      whenReady(result) { exp =>
        exp mustBe a[Exception]
      }
    }
  }

  "must return InternalServerError when getRegistrationClientDetails returns Left" in {

    when(mockAccountService.getPreviousRegistrations()(any()))
      .thenReturn(Seq(previousRegistration).toFuture)

    when(mockAccountService.getRegistrationClientDetails(any())(any()))
      .thenReturn(Left(InternalServerError).toFuture)

    val application = applicationBuilder(userAnswers = Some(emptyUserAnswersWithVatInfo))
      .overrides(bind[AccountService].toInstance(mockAccountService))
      .build()

    running(application) {
      val request = FakeRequest(GET, viewOrChangePreviousRegistrationRoute)

      val result = route(application, request).value

      status(result) mustBe INTERNAL_SERVER_ERROR
    }
  }

}