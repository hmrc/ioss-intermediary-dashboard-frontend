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

package controllers.saveForLater

import base.SpecBase
import config.FrontendAppConfig
import connectors.RegistrationConnector
import forms.saveForLater.ContinueSingleClientSavedReturnFormProvider
import models.UserAnswers
import models.etmp.{EtmpClientDetails, RegistrationWrapper}
import models.saveForLater.{ContinueSingleClientSavedReturn, SavedUserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.{times, verify, when}
import org.scalacheck.Gen
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.saveForLater.ContinueSingleClientSavedReturnPage
import play.api.data.Form
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.SaveForLaterService
import utils.FutureSyntax.FutureOps
import views.html.saveForLater.ContinueSingleClientSavedReturnView

class ContinueSingleClientSavedReturnControllerSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  private val mockSaveForLaterService: SaveForLaterService = mock[SaveForLaterService]
  private val mockRegistrationConnector: RegistrationConnector = mock[RegistrationConnector]

  private val registrationWrapper: RegistrationWrapper = arbitraryRegistrationWrapper.arbitrary.sample.value
  private val etmpClientDetails: Seq[EtmpClientDetails] = registrationWrapper.etmpDisplayRegistration.clientDetails
  private val savedUserAnswers: Seq[SavedUserAnswers] = Gen.listOfN(3, arbitrarySavedUserAnswers.arbitrary).sample.value

  private val indexedClientIossNumbers: Seq[(String, Int)] = etmpClientDetails.map(_.clientIossID).zipWithIndex
  private val mappedSavedUserAnswers: Seq[SavedUserAnswers] = savedUserAnswers.zipWithIndex.map { (savedAnswers, index) =>
    savedAnswers.copy(iossNumber = indexedClientIossNumbers(index)._1)
  }

  private val formProvider = new ContinueSingleClientSavedReturnFormProvider()
  private val form: Form[ContinueSingleClientSavedReturn] = formProvider(etmpClientDetails.head.clientName, etmpClientDetails.head.clientIossID)

  private lazy val continueSingleClientSavedReturnRoute: String =
    routes.ContinueSingleClientSavedReturnController.onPageLoad(waypoints, etmpClientDetails.head.clientIossID).url

  override def beforeEach(): Unit = {
    Mockito.reset(
      mockSaveForLaterService,
      mockRegistrationConnector
    )
  }

  "ContinueSingleClientSavedReturn Controller" - {

    "must return OK and the correct view for a GET" in {

      when(mockSaveForLaterService.getAllSavedReturnsForClient(any())(any())) thenReturn mappedSavedUserAnswers.toFuture
      when(mockRegistrationConnector.getRegistration(any())(any())) thenReturn registrationWrapper.toFuture

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[SaveForLaterService].toInstance(mockSaveForLaterService),
          bind[RegistrationConnector].toInstance(mockRegistrationConnector)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, continueSingleClientSavedReturnRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ContinueSingleClientSavedReturnView]

        status(result) `mustBe` OK
        contentAsString(result) `mustBe` view(form, waypoints, etmpClientDetails.head.clientName, etmpClientDetails.head.clientIossID)(request, messages(application)).toString
        verify(mockSaveForLaterService, times(1)).getAllSavedReturnsForClient(any())(any())
        verify(mockRegistrationConnector, times(1)).getRegistration(any())(any())
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      when(mockSaveForLaterService.getAllSavedReturnsForClient(any())(any())) thenReturn mappedSavedUserAnswers.toFuture
      when(mockRegistrationConnector.getRegistration(any())(any())) thenReturn registrationWrapper.toFuture

      val userAnswers = UserAnswers(userAnswersId)
        .set(ContinueSingleClientSavedReturnPage(etmpClientDetails.head.clientIossID), ContinueSingleClientSavedReturn.values.head).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[SaveForLaterService].toInstance(mockSaveForLaterService),
          bind[RegistrationConnector].toInstance(mockRegistrationConnector)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, continueSingleClientSavedReturnRoute)

        val view = application.injector.instanceOf[ContinueSingleClientSavedReturnView]

        val result = route(application, request).value

        status(result) `mustBe` OK
        contentAsString(result) `mustBe` view(form.fill(ContinueSingleClientSavedReturn.values.head), waypoints, etmpClientDetails.head.clientName, etmpClientDetails.head.clientIossID)(request, messages(application)).toString
        verify(mockSaveForLaterService, times(1)).getAllSavedReturnsForClient(any())(any())
        verify(mockRegistrationConnector, times(1)).getRegistration(any())(any())
      }
    }

    "must redirect to the next page when the user answers Yes" in {

      when(mockSaveForLaterService.getAllSavedReturnsForClient(any())(any())) thenReturn mappedSavedUserAnswers.toFuture
      when(mockRegistrationConnector.getRegistration(any())(any())) thenReturn registrationWrapper.toFuture

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn true.toFuture

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[SaveForLaterService].toInstance(mockSaveForLaterService),
            bind[RegistrationConnector].toInstance(mockRegistrationConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, continueSingleClientSavedReturnRoute)
            .withFormUrlEncodedBody(("value", ContinueSingleClientSavedReturn.values.head.toString))

        val result = route(application, request).value

        val config = application.injector.instanceOf[FrontendAppConfig]

        val redirectUrl: String = s"${config.startIntermediarySavedReturns}/${etmpClientDetails.head.clientIossID}/${mappedSavedUserAnswers.head.period}"

        status(result) `mustBe` SEE_OTHER
        redirectLocation(result).value `mustBe` redirectUrl
        verify(mockSaveForLaterService, times(1)).getAllSavedReturnsForClient(any())(any())
        verify(mockRegistrationConnector, times(1)).getRegistration(any())(any())
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      when(mockSaveForLaterService.getAllSavedReturnsForClient(any())(any())) thenReturn mappedSavedUserAnswers.toFuture
      when(mockRegistrationConnector.getRegistration(any())(any())) thenReturn registrationWrapper.toFuture

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[SaveForLaterService].toInstance(mockSaveForLaterService),
          bind[RegistrationConnector].toInstance(mockRegistrationConnector)
        ).build()

      running(application) {
        val request =
          FakeRequest(POST, continueSingleClientSavedReturnRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[ContinueSingleClientSavedReturnView]

        val result = route(application, request).value

        status(result) `mustBe` BAD_REQUEST
        contentAsString(result) `mustBe` view(boundForm, waypoints, etmpClientDetails.head.clientName, etmpClientDetails.head.clientIossID)(request, messages(application)).toString
        verify(mockSaveForLaterService, times(1)).getAllSavedReturnsForClient(any())(any())
        verify(mockRegistrationConnector, times(1)).getRegistration(any())(any())
      }
    }
  }
}
