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
import models.responses.InternalServerError
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.{EmptyWaypoints, Waypoints}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.YourAccountView
import utils.FutureSyntax.FutureOps


class YourAccountControllerSpec extends SpecBase with MockitoSugar {

  private val waypoints: Waypoints = EmptyWaypoints
  private val businessName = "Company name"
  private val intermediaryNumber = "IN9001234567"
  private val newMessage = 0

  lazy val yourAccountRoute: String = routes.YourAccountController.onPageLoad(waypoints).url

  "YourAccount Controller" - {

    "should display your account view" - {

      "must return OK and the correct view for a GET" in {

        val mockRegistrationConnector = mock[RegistrationConnector]

        when(mockRegistrationConnector.getNumberOfPendingRegistrations(any())(any()))
          .thenReturn(1.toLong.toFuture)
        when(mockRegistrationConnector.getVatCustomerInfo(any())(any()))
          .thenReturn(Right(vatCustomerInfo).toFuture)

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[RegistrationConnector].toInstance(mockRegistrationConnector))
          .build()
        val appConfig = application.injector.instanceOf[FrontendAppConfig]

        running(application) {
          val request = FakeRequest(GET, yourAccountRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[YourAccountView]

          status(result) `mustEqual` OK
          contentAsString(result) mustEqual view(
            waypoints,
            businessName,
            intermediaryNumber,
            newMessage,
            appConfig.addClientUrl,
            appConfig.viewClientsListUrl,
            appConfig.changeYourRegistrationUrl,
            1,
            appConfig.redirectToPendingClientsPage,
            appConfig.leaveThisServiceUrl
          )(request, messages(application)).toString
        }
      }
    }

    "must throw an exception and log the error when an unexpected error is returned" in {
      
      val mockRegistrationConnector = mock[RegistrationConnector]

      when(mockRegistrationConnector.getNumberOfPendingRegistrations(any())(any()))
        .thenReturn(0.toLong.toFuture)
      when(mockRegistrationConnector.getVatCustomerInfo(any())(any()))
        .thenReturn(Left(InternalServerError).toFuture)

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[RegistrationConnector].toInstance(mockRegistrationConnector))
        .build()

      running(application) {
        val request = FakeRequest(GET, yourAccountRoute)

        val thrown = intercept[Exception] {
          await(route(application, request).value)
        }

        thrown.getMessage must include("Internal server error")
      }
    }
  }
}
