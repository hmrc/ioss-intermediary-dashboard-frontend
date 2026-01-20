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

package controllers.amend

import base.SpecBase
import config.FrontendAppConfig
import models.responses.InternalServerError
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.{EmptyWaypoints, Waypoints}
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.test.Helpers.*
import play.api.test.{FakeRequest, Helpers}
import queries.PreviousRegistrationIntermediaryNumberQuery
import services.intermediaries.AccountService
import utils.FutureSyntax.FutureOps
import viewmodels.amend.ViewOrChangePreviousRegistrationViewModel
import views.html.ViewOrChangePreviousRegistrationView

class ViewOrChangePreviousRegistrationControllerSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  private val waypoints: Waypoints = EmptyWaypoints

  private val mockAccountService: AccountService = mock[AccountService]

  private val clientDetails = registrationWrapper.etmpDisplayRegistration.clientDetails

  override def beforeEach(): Unit =
    Mockito.reset(mockAccountService)

    "ViewOrChangePreviousRegistration Controller" - {

      "must return OK and the correct view for a GET" in {

        val userAnswers =
          emptyUserAnswersWithVatInfo
            .set(PreviousRegistrationIntermediaryNumberQuery, intermediaryNumber)
            .success
            .value

        when(mockAccountService.getRegistrationClientDetails(eqTo(intermediaryNumber))(any()))
          .thenReturn(Right(clientDetails).toFuture)

        val application =
          applicationBuilder(userAnswers = Some(userAnswers))
            .overrides(bind[AccountService].toInstance(mockAccountService))
            .build()

        running(application) {
          val request = FakeRequest(GET, routes.ViewOrChangePreviousRegistrationController.onPageLoad(waypoints).url)

          implicit val msgs: Messages = messages(application)

          val appConfig = application.injector.instanceOf[FrontendAppConfig]

          val returnToCurrentRegUrl = appConfig.viewClientsListUrl

          val changeRegistrationRedirectUrl = appConfig.changeYourNetpRegistrationUrl

          val viewModel = ViewOrChangePreviousRegistrationViewModel(clientDetails, changeRegistrationRedirectUrl)

          val result = route(application, request).value

          val view = application.injector.instanceOf[ViewOrChangePreviousRegistrationView]

          status(result) mustBe OK
          contentAsString(result) mustBe view(
            waypoints,
            intermediaryNumber,
            viewModel,
            returnToCurrentRegUrl
          )(request).toString
        }
      }

      "must throw IllegalStateException when intermediary number is missing from userAnswers" in {

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswersWithVatInfo))
            .overrides(bind[AccountService].toInstance(mockAccountService))
            .build()

        running(application) {
          val request = FakeRequest(GET, routes.ViewOrChangePreviousRegistrationController.onPageLoad(waypoints).url)
          val result = route(application, request).value.failed

          whenReady(result) { ex =>
            ex mustBe a[IllegalStateException]
            ex.getMessage must include("Intermediary number missing")
          }
        }
      }

      "must return InternalServerError when getRegistrationClientDetails returns Left" in {

        val userAnswers =
          emptyUserAnswersWithVatInfo
            .set(PreviousRegistrationIntermediaryNumberQuery, intermediaryNumber)
            .success
            .value

        when(mockAccountService.getRegistrationClientDetails(eqTo(intermediaryNumber))(any()))
          .thenReturn(Left(InternalServerError).toFuture)

        val application =
          applicationBuilder(userAnswers = Some(userAnswers))
            .overrides(bind[AccountService].toInstance(mockAccountService))
            .build()

        running(application) {
          val request = FakeRequest(GET, routes.ViewOrChangePreviousRegistrationController.onPageLoad(waypoints).url)

          val result = route(application, request).value

          status(result) mustBe INTERNAL_SERVER_ERROR
        }
      }
    }
}