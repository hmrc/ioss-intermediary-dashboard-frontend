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

package controllers.test

import base.SpecBase
import connectors.test.TestOnlySecureMessagingConnector
import forms.test.TestOnlySecureMessagingFormProvider
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import play.api.data.Form
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.TestOnlySecureMessagingView
import org.mockito.ArgumentMatchers.any
import uk.gov.hmrc.http.HttpResponse
import play.api.inject.bind

import scala.concurrent.Future
import scala.util.Random


class TestOnlySecureMessagingControllerSpec extends SpecBase with MockitoSugar {

  lazy val testOnlySecureMessagingRoute: String = routes.TestOnlySecureMessagingController.onPageLoad().url
  val form: Form[TestOnlySecureMessagingFormProvider] = TestOnlySecureMessagingFormProvider()
  "TestOnlySecureMessaging Controller" - {

    ".onPageLoad" - {
      "must return OK and the correct view for a GET" in {

        val application = applicationBuilder()
          .configure("application.router" -> "testOnlyDoNotUseInAppConf.Routes")
          .build()

        running(application) {
          val request = FakeRequest(GET, testOnlySecureMessagingRoute)

          val view = application.injector.instanceOf[TestOnlySecureMessagingView]

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form)(request, messages(application)).toString
        }
      }
    }

    ".onSubmit" - {
      "must generate one unread message when form is submitted requesting one message" in {

        val mockConnector = mock[TestOnlySecureMessagingConnector]
        when(mockConnector.createBulkMessages()(any()))
          .thenReturn(Future.successful(HttpResponse(201, "")))

        val application = applicationBuilder()
          .overrides(bind[TestOnlySecureMessagingConnector].toInstance(mockConnector))
          .configure("application.router" -> "testOnlyDoNotUseInAppConf.Routes")
          .build()

        running(application) {
          val request = FakeRequest(
            POST,
            routes.TestOnlySecureMessagingController.onSubmit().url
          ).withFormUrlEncodedBody(
            "numberOfMessages" -> "1",
            "isReadMessage" -> "false"
          )

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) must include("Messages successfully created!")
          contentAsString(result) must include("Number of messages created: 1")


          verify(mockConnector, times(1)).createBulkMessages()(any())
        }
      }

      "must generate multiple unread messages when form is submitted requesting more than one message" in {

        val mockConnector = mock[TestOnlySecureMessagingConnector]
        when(mockConnector.createBulkMessages()(any()))
          .thenReturn(Future.successful(HttpResponse(201, "")))

        val application = applicationBuilder()
          .overrides(bind[TestOnlySecureMessagingConnector].toInstance(mockConnector))
          .configure("application.router" -> "testOnlyDoNotUseInAppConf.Routes")
          .build()

        val randomNumberOfMessagesToCreate: Int = Random.between(2, 51)

        running(application) {
          val request = FakeRequest(
            POST,
            routes.TestOnlySecureMessagingController.onSubmit().url
          ).withFormUrlEncodedBody(
            "numberOfMessages" -> s"$randomNumberOfMessagesToCreate",
            "isReadMessage" -> "false"
          )

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) must include("Messages successfully created!")
          contentAsString(result) must include(s"Number of messages created: $randomNumberOfMessagesToCreate")

          verify(mockConnector, times(randomNumberOfMessagesToCreate)).createBulkMessages()(any())
        }
      }

      "must return InternalServerError when connector doesn't return a 201" in {
        val mockConnector = mock[TestOnlySecureMessagingConnector]
        when(mockConnector.createBulkMessages()(any()))
          .thenReturn(Future.successful(HttpResponse(500, "")))

        val application = applicationBuilder()
          .overrides(bind[TestOnlySecureMessagingConnector].toInstance(mockConnector))
          .configure("application.router" -> "testOnlyDoNotUseInAppConf.Routes")
          .build()

        running(application) {
          val request = FakeRequest(
            POST,
            routes.TestOnlySecureMessagingController.onSubmit().url
          ).withFormUrlEncodedBody(
            "numberOfMessages" -> "1",
            "isReadMessage" -> "false"
          )

          val result = route(application, request).value

          status(result) mustEqual INTERNAL_SERVER_ERROR
          contentAsString(result) must include("Error creating messages")
        }

      }
    }
  }
}