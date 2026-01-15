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
import config.FrontendAppConfig
import controllers.routes
import models.etmp.EtmpClientDetails
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.ClientReturnService
import viewmodels.clientList.ClientReturnsListViewModel
import views.html.ClientReturnsListView
import utils.FutureSyntax.FutureOps

class ClientReturnsListControllerSpec extends SpecBase {

  private val mockClientReturnService: ClientReturnService = mock[ClientReturnService]
  private val registration = registrationWrapper.etmpDisplayRegistration
  private val clientDetailsList: Seq[EtmpClientDetails] = registration.clientDetails
  private val navigateToPreviousRegistrationsListUrl = routes.ClientsPreviousRegistrationListController.onPageLoad(waypoints).url

  "ClientReturnsList Controller" - {

    "must return OK and the correct view for a GET" in {

      when(mockClientReturnService.clientsWithCompletedReturns(
        eqTo(clientDetailsList),
        eqTo(intermediaryNumber)
      )(any())) thenReturn clientDetailsList.toFuture

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[ClientReturnService].toInstance(mockClientReturnService))
        .build()

      running(application) {
        implicit val msgs: Messages = messages(application)

        val request = FakeRequest(GET, routes.ClientReturnsListController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ClientReturnsListView]

        val config = application.injector.instanceOf[FrontendAppConfig]

        val clientReturnsListViewModel: ClientReturnsListViewModel = ClientReturnsListViewModel(clientDetailsList, config.startReturnsHistoryUrl)

        status(result) `mustBe` OK
        contentAsString(result) `mustBe` view(clientReturnsListViewModel, navigateToPreviousRegistrationsListUrl)(request).toString
      }
    }
  }
}
