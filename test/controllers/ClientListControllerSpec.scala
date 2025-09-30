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
import models.etmp.EtmpClientDetails
import models.responses.*
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito
import org.mockito.Mockito.{times, verify, when}
import org.scalacheck.Gen
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import utils.FutureSyntax.FutureOps
import viewmodels.clientList.ClientListViewModel
import views.html.ClientListView

class ClientListControllerSpec extends SpecBase with BeforeAndAfterEach {

  private val mockRegistrationConnector: RegistrationConnector = mock[RegistrationConnector]

  private val etmpClientDetails: Seq[EtmpClientDetails] = Gen
    .listOfN(6, arbitraryEtmpClientDetails.arbitrary).sample.value

  override def beforeEach(): Unit = {
    Mockito.reset(
      mockRegistrationConnector
    )
  }

  "ClientList Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[RegistrationConnector].toInstance(mockRegistrationConnector))
        .build()

      when(mockRegistrationConnector.getDisplayRegistration(any())(any())) thenReturn Right(etmpClientDetails).toFuture

      running(application) {
        implicit val msgs: Messages = messages(application)

        val request = FakeRequest(GET, routes.ClientListController.onPageLoad().url)

        val config = application.injector.instanceOf[FrontendAppConfig]

        val result = route(application, request).value

        val view = application.injector.instanceOf[ClientListView]

        val clientListViewModel: ClientListViewModel = ClientListViewModel(
          clientList = etmpClientDetails,
          changeClientRegistrationUrl = config.changeYourNetpRegistrationUrl,
          excludeClientUrl = config.leaveNetpServiceUrl
        )

        status(result) `mustBe` OK
        contentAsString(result) `mustBe` view(clientListViewModel)(request).toString
        verify(mockRegistrationConnector, times(1)).getDisplayRegistration(eqTo(intermediaryNumber))(any())
      }
    }

    Seq(NotFound, InvalidJson, InternalServerError, RegistrationNotFound, ConflictFound).foreach { errorResponse =>
      s"must throw an Exception for a GET when the Registration connector returns the error: $errorResponse" in {

        val errorMessage: String = s"There was a problem retrieving Client Details with error: ${errorResponse.body}"

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[RegistrationConnector].toInstance(mockRegistrationConnector))
          .build()

        when(mockRegistrationConnector.getDisplayRegistration(any())(any())) thenReturn Left(errorResponse).toFuture

        running(application) {
          val request = FakeRequest(GET, routes.ClientListController.onPageLoad().url)

          val result = route(application, request).value

          whenReady(result.failed) { exp =>
            exp `mustBe` a[Exception]
            exp.getMessage `mustBe` errorMessage
          }
          verify(mockRegistrationConnector, times(1)).getDisplayRegistration(eqTo(intermediaryNumber))(any())
        }
      }
    }
  }
}
