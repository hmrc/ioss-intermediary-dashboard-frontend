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

package controllers.amend

import base.SpecBase
import models.amend.PreviousRegistration
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.when
import org.scalacheck.Gen
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.amend.{ViewOrChangePreviousRegistrationPage, ViewOrChangePreviousRegistrationsMultiplePage}
import pages.{EmptyWaypoints, Waypoints}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.intermediaries.AccountService
import utils.FutureSyntax.FutureOps


class ViewOrChangePreviousRegistrationsStartControllerSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  private val waypoints: Waypoints = EmptyWaypoints

  private val mockAccountService: AccountService = mock[AccountService]
  private val previousRegistrations: Seq[PreviousRegistration] = Gen.listOfN(4, arbitraryPreviousRegistration.arbitrary).sample.value


  override def beforeEach(): Unit =
    Mockito.reset(mockAccountService)

  "ViewOrChangePreviousRegistrationsStart Controller" - {

    "must redirect to ViewOrChangePreviousRegistrationPage when one previous registration exists" in {

      val singleRegistration = Seq(previousRegistrations.head)

      val mockSessionRepository = mock[SessionRepository]

      when(mockAccountService.getPreviousRegistrations()(any())).thenReturn(singleRegistration.toFuture)

      when(mockSessionRepository.set(any())).thenReturn(true.toFuture)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswersWithVatInfo))
          .overrides(
            bind[AccountService].toInstance(mockAccountService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(GET, routes.ViewOrChangePreviousRegistrationsStartController.onPageLoad(waypoints).url)

        val result = route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe ViewOrChangePreviousRegistrationPage.route(waypoints).url

      }
    }

    "must redirect to ViewOrChangePreviousRegistrationsMultiplePage when more than one previous registration exists" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockAccountService.getPreviousRegistrations()(any())).thenReturn(previousRegistrations.toFuture)

      when(mockSessionRepository.set(any())).thenReturn(true.toFuture)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswersWithVatInfo))
          .overrides(
            bind[AccountService].toInstance(mockAccountService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(GET, routes.ViewOrChangePreviousRegistrationsStartController.onPageLoad(waypoints).url)

        val result = route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe ViewOrChangePreviousRegistrationsMultiplePage.route(waypoints).url

      }
    }

    "must throw IllegalStateException when no previous registrations exist" in {

      when(mockAccountService.getPreviousRegistrations()(any()))
        .thenReturn(Seq.empty.toFuture)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswersWithVatInfo))
          .overrides(bind[AccountService].toInstance(mockAccountService))
          .build()

      running(application) {
        val request =
          FakeRequest(GET, routes.ViewOrChangePreviousRegistrationsStartController.onPageLoad(waypoints).url)

        val result = route(application, request).value.failed

        whenReady(result) { ex =>
          ex mustBe a[IllegalStateException]
        }
      }
    }

  }
}