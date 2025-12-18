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

package controllers.saveForLater

import base.SpecBase
import connectors.RegistrationConnector
import forms.saveForLater.SelectClientSavedReturnFormProvider
import models.UserAnswers
import models.etmp.{EtmpClientDetails, RegistrationWrapper}
import models.saveForLater.SavedUserAnswers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.{times, verify, when}
import org.scalacheck.Gen
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.saveForLater.SelectClientSavedReturnPage
import play.api.data.Form
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.SaveForLaterService
import utils.FutureSyntax.FutureOps
import views.html.saveForLater.SelectClientSavedReturnView

class SelectClientSavedReturnControllerSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  private val mockSaveForLaterService: SaveForLaterService = mock[SaveForLaterService]
  private val mockRegistrationConnector: RegistrationConnector = mock[RegistrationConnector]

  private val registrationWrapper: RegistrationWrapper = arbitraryRegistrationWrapper.arbitrary.sample.value
  private val etmpClientDetails: Seq[EtmpClientDetails] = registrationWrapper.etmpDisplayRegistration.clientDetails
  private val savedUserAnswers: Seq[SavedUserAnswers] = Gen.listOfN(3, arbitrarySavedUserAnswers.arbitrary).sample.value

  private val indexedClientIossNumbers: Seq[(String, Int)] = etmpClientDetails.map(_.clientIossID).zipWithIndex
  private val mappedSavedUserAnswers: Seq[SavedUserAnswers] = savedUserAnswers.zipWithIndex.map { (savedAnswers, index) =>
    savedAnswers.copy(iossNumber = indexedClientIossNumbers(index)._1)
  }

  private val formProvider = new SelectClientSavedReturnFormProvider()
  private val form: Form[EtmpClientDetails] = formProvider(etmpClientDetails)

  private lazy val selectClientSavedReturnRoute: String = routes.SelectClientSavedReturnController.onPageLoad(waypoints).url

  override def beforeEach(): Unit = {
    Mockito.reset(
      mockSaveForLaterService,
      mockRegistrationConnector
    )
  }

  "SelectClientSavedReturn Controller" - {

    "must return OK and the correct view for a GET" in {

      when(mockSaveForLaterService.getAllClientSavedAnswers()(any())) thenReturn mappedSavedUserAnswers.toFuture
      when(mockRegistrationConnector.getRegistration(any())(any())) thenReturn registrationWrapper.toFuture

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[SaveForLaterService].toInstance(mockSaveForLaterService),
          bind[RegistrationConnector].toInstance(mockRegistrationConnector)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, selectClientSavedReturnRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SelectClientSavedReturnView]

        status(result) `mustBe` OK
        contentAsString(result) `mustBe` view(form, waypoints, etmpClientDetails)(request, messages(application)).toString
        verify(mockSaveForLaterService, times(1)).getAllClientSavedAnswers()(any())
        verify(mockRegistrationConnector, times(1)).getRegistration(any())(any())
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      when(mockSaveForLaterService.getAllClientSavedAnswers()(any())) thenReturn mappedSavedUserAnswers.toFuture
      when(mockRegistrationConnector.getRegistration(any())(any())) thenReturn registrationWrapper.toFuture

      val userAnswers = UserAnswers(userAnswersId)
        .set(SelectClientSavedReturnPage, etmpClientDetails.head).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[SaveForLaterService].toInstance(mockSaveForLaterService),
          bind[RegistrationConnector].toInstance(mockRegistrationConnector)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, selectClientSavedReturnRoute)

        val view = application.injector.instanceOf[SelectClientSavedReturnView]

        val result = route(application, request).value

        status(result) `mustBe` OK
        contentAsString(result) `mustBe` view(form.fill(etmpClientDetails.head), waypoints, etmpClientDetails)(request, messages(application)).toString
        verify(mockSaveForLaterService, times(1)).getAllClientSavedAnswers()(any())
        verify(mockRegistrationConnector, times(1)).getRegistration(any())(any())
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      when(mockSaveForLaterService.getAllClientSavedAnswers()(any())) thenReturn mappedSavedUserAnswers.toFuture
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
          FakeRequest(POST, selectClientSavedReturnRoute)
            .withFormUrlEncodedBody(("value", etmpClientDetails.head.clientIossID))

        val result = route(application, request).value

        val expectedAnswers: UserAnswers = emptyUserAnswers
          .set(SelectClientSavedReturnPage, etmpClientDetails.head).success.value

        status(result) `mustBe` SEE_OTHER
        redirectLocation(result).value `mustBe` SelectClientSavedReturnPage.navigate(waypoints, emptyUserAnswers, expectedAnswers).url
        verify(mockSaveForLaterService, times(1)).getAllClientSavedAnswers()(any())
        verify(mockRegistrationConnector, times(1)).getRegistration(any())(any())
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      when(mockSaveForLaterService.getAllClientSavedAnswers()(any())) thenReturn mappedSavedUserAnswers.toFuture
      when(mockRegistrationConnector.getRegistration(any())(any())) thenReturn registrationWrapper.toFuture

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[SaveForLaterService].toInstance(mockSaveForLaterService),
          bind[RegistrationConnector].toInstance(mockRegistrationConnector)
        )
        .build()

      running(application) {
        val request =
          FakeRequest(POST, selectClientSavedReturnRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[SelectClientSavedReturnView]

        val result = route(application, request).value

        status(result) `mustBe` BAD_REQUEST
        contentAsString(result) `mustBe` view(boundForm, waypoints, etmpClientDetails)(request, messages(application)).toString
        verify(mockSaveForLaterService, times(1)).getAllClientSavedAnswers()(any())
        verify(mockRegistrationConnector, times(1)).getRegistration(any())(any())
      }
    }
  }
}
