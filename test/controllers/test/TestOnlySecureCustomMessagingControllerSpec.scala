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

package controllers.test

import base.SpecBase
import connectors.test.TestOnlySecureMessagingConnector
import forms.test.TestOnlySecureCustomMessagingFormProvider
import org.mockito.Mockito.{times, verify, when}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.scalatestplus.mockito.MockitoSugar
import play.api.data.Form
import play.api.inject
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.http.HttpResponse
import views.html.TestOnlySecureCustomMessagingView

import scala.concurrent.Future


class TestOnlySecureCustomMessagingControllerSpec extends SpecBase with MockitoSugar {

  lazy val testOnlySecureCustomMessagingRoute: String = routes.TestOnlySecureCustomMessagingController.onPageLoad().url
  val form: Form[TestOnlySecureCustomMessagingFormProvider] = TestOnlySecureCustomMessagingFormProvider()

  "TestOnlySecureCustomMessaging Controller" - {

    ".onPageLoad" - {
      "must return OK and the correct view for a GET" in {

        val application = applicationBuilder()
          .configure("application.router" -> "testOnlyDoNotUseInAppConf.Routes")
          .build()

        running(application) {
          val request = FakeRequest(GET, testOnlySecureCustomMessagingRoute)

          val view = application.injector.instanceOf[TestOnlySecureCustomMessagingView]

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form)(request, messages(application)).toString
        }
      }
    }

    ".onSubmit" - {

      "must create a custom message and return successful HTML response" in {
        val mockConnector = mock[TestOnlySecureMessagingConnector]
        when(mockConnector.createCustomMessage(any(), any(), any(), any(), any(), any(), any())(any()))
          .thenReturn(Future.successful(HttpResponse(201, "")))

        val application = applicationBuilder()
          .overrides(bind[TestOnlySecureMessagingConnector].toInstance(mockConnector))
          .configure("application.router" -> "testOnlyDoNotUseInAppConf.Routes")
          .build()


        running(application) {
          val request = FakeRequest(
            POST,
            routes.TestOnlySecureCustomMessagingController.onSubmit().url
          ).withFormUrlEncodedBody(
            "enrolmentKey" -> enrolmentKey,
            "identifierValue" -> identifierValue,
            "firstName" -> "firstName",
            "lastName" -> "lastName",
            "emailAddress" -> "test@email.com",
            "subject" -> "test subject",
            "body" -> "test body"
          )

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) must include("Message successfully created!")

          verify(mockConnector, times(1))
            .createCustomMessage(
              eqTo(enrolmentKey), eqTo(identifierValue), eqTo("firstName"), eqTo("lastName"), eqTo("test@email.com"), eqTo("test subject"), eqTo("test body"))(any())
        }
      }

      "must return InternalServerError and return HTML response when connector doesn't return a 201" in {
        val mockConnector = mock[TestOnlySecureMessagingConnector]
        when(mockConnector.createCustomMessage(any(), any(), any(), any(), any(), any(), any())(any()))
          .thenReturn(Future.successful(HttpResponse(500, "")))

        val application = applicationBuilder()
          .overrides(bind[TestOnlySecureMessagingConnector].toInstance(mockConnector))
          .configure("application.router" -> "testOnlyDoNotUseInAppConf.Routes")
          .build()

        running(application) {
          val request = FakeRequest(
            POST,
            routes.TestOnlySecureCustomMessagingController.onSubmit().url
          ).withFormUrlEncodedBody(
            "enrolmentKey" -> enrolmentKey,
            "identifierValue" -> identifierValue,
            "firstName" -> "firstName",
            "lastName" -> "lastName",
            "emailAddress" -> "test@email.com",
            "subject" -> "test subject",
            "body" -> "test body"
          )

          val result = route(application, request).value

          status(result) mustEqual INTERNAL_SERVER_ERROR
          contentAsString(result) must include("Error creating message")
        }

      }
    }
  }
}