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

package controllers.returns

import base.SpecBase
import config.FrontendAppConfig
import models.etmp.{EtmpClientDetails, EtmpDisplayRegistration}
import models.returns.SubmissionStatus.{Due, Overdue}
import models.returns.{CurrentReturns, Return}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.{times, verify, when}
import org.scalacheck.Gen
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.returns.CurrentReturnsService
import utils.FutureSyntax.FutureOps
import viewmodels.returns.ClientOutstandingReturnsListViewModel
import views.html.returns.ClientsOutstandingReturnsListView

class ClientsOutstandingReturnsListControllerSpec extends SpecBase with BeforeAndAfterEach {

  private val mockCurrentReturnsService: CurrentReturnsService = mock[CurrentReturnsService]

  private val etmpDisplayRegistration: EtmpDisplayRegistration = arbitraryEtmpDisplayRegistration.arbitrary.sample.value

  private val allClientIossNumbers: Seq[String] = etmpDisplayRegistration.clientDetails.map(_.clientIossID)

  private val currentReturns: Seq[CurrentReturns] = Gen.listOfN(3, arbitraryCurrentReturns.arbitrary).sample.value
  private val incompleteDueReturns: Seq[Return] = Gen.listOfN(3, arbitraryReturn.arbitrary).sample.value.map(_.copy(submissionStatus = Due))
  private val incompleteOverdueReturns: Seq[Return] = Gen.listOfN(3, arbitraryReturn.arbitrary).sample.value.map(_.copy(submissionStatus = Overdue))

  private val indexedClientIossNumbers: Seq[(String, Int)] = allClientIossNumbers.zipWithIndex
  private val updatedCurrentReturns: Seq[CurrentReturns] = currentReturns.zipWithIndex.map { (currentReturn, index) =>
    currentReturn.copy(iossNumber = indexedClientIossNumbers(index)._1)
      .copy(incompleteReturns = incompleteDueReturns ++ incompleteOverdueReturns)
  }

  protected lazy val clientsOutstandingReturnsRoute: String = routes.ClientsOutstandingReturnsListController.onPageLoad().url

  override def beforeEach(): Unit = {
    Mockito.reset(mockCurrentReturnsService)
  }

  "ClientsOutstandingReturnsListController" - {

    "must return OK and the correct view for a GET when there are outstanding client returns present" in {

      when(mockCurrentReturnsService.getCurrentReturns(any())(any())) thenReturn updatedCurrentReturns.toFuture

      val application = applicationBuilder(
        userAnswers = Some(emptyUserAnswers),
        registrationWrapper = registrationWrapper.copy(etmpDisplayRegistration = etmpDisplayRegistration)
      )
        .overrides(
          bind[CurrentReturnsService].toInstance(mockCurrentReturnsService)
        )
        .build()

      running(application) {
        implicit val msgs: Messages = messages(application)

        val request = FakeRequest(GET, clientsOutstandingReturnsRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ClientsOutstandingReturnsListView]

        val config = application.injector.instanceOf[FrontendAppConfig]

        val clientDetails: Seq[EtmpClientDetails] = etmpDisplayRegistration.clientDetails

        val viewModel: ClientOutstandingReturnsListViewModel =
          ClientOutstandingReturnsListViewModel(clientDetails, config.startCurrentReturnUrl)
        val link: String = routes.ClientsOverdueReturnsListController.onPageLoad(waypoints).url

        status(result) `mustBe` OK
        contentAsString(result) `mustBe` view(
          viewModel,
          link,
          currentReturnsNonEmpty = true,
          overdueReturnsNonEmpty = true
        )(request, messages(application)).toString
        verify(mockCurrentReturnsService, times(1)).getCurrentReturns(any())(any())
      }
    }

    "must return OK and the correct view for a GET when there are no client Overdue returns present" in {

      val updatedCurrentReturnsWithoutOverdueReturns: Seq[CurrentReturns] = currentReturns.zipWithIndex.map { (currentReturn, index) =>
        currentReturn.copy(iossNumber = indexedClientIossNumbers(index)._1)
          .copy(incompleteReturns = incompleteDueReturns)
      }

      when(mockCurrentReturnsService.getCurrentReturns(any())(any())) thenReturn updatedCurrentReturnsWithoutOverdueReturns.toFuture

      val application = applicationBuilder(
        userAnswers = Some(emptyUserAnswers),
        registrationWrapper = registrationWrapper.copy(etmpDisplayRegistration = etmpDisplayRegistration)
      )
        .overrides(
          bind[CurrentReturnsService].toInstance(mockCurrentReturnsService)
        )
        .build()

      running(application) {
        implicit val msgs: Messages = messages(application)

        val request = FakeRequest(GET, clientsOutstandingReturnsRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ClientsOutstandingReturnsListView]

        val config = application.injector.instanceOf[FrontendAppConfig]

        val clientDetails: Seq[EtmpClientDetails] = etmpDisplayRegistration.clientDetails

        val viewModel: ClientOutstandingReturnsListViewModel =
          ClientOutstandingReturnsListViewModel(clientDetails, config.startCurrentReturnUrl)
        val link: String = routes.ClientsOverdueReturnsListController.onPageLoad(waypoints).url

        status(result) `mustBe` OK
        contentAsString(result) `mustBe` view(
          viewModel,
          link,
          currentReturnsNonEmpty = true,
          overdueReturnsNonEmpty = false
        )(request, messages(application)).toString
        verify(mockCurrentReturnsService, times(1)).getCurrentReturns(any())(any())
      }
    }

    "must return OK and the correct view for a GET when no outstanding client returns present" in {

      when(mockCurrentReturnsService.getCurrentReturns(any())(any())) thenReturn currentReturns.toFuture

      val application = applicationBuilder(
        userAnswers = Some(emptyUserAnswers),
        registrationWrapper = registrationWrapper.copy(etmpDisplayRegistration = etmpDisplayRegistration)
      )
        .overrides(
          bind[CurrentReturnsService].toInstance(mockCurrentReturnsService)
        )
        .build()

      running(application) {
        implicit val msgs: Messages = messages(application)

        val request = FakeRequest(GET, clientsOutstandingReturnsRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ClientsOutstandingReturnsListView]

        val config = application.injector.instanceOf[FrontendAppConfig]

        val viewModel: ClientOutstandingReturnsListViewModel =
          ClientOutstandingReturnsListViewModel(Seq.empty, config.startCurrentReturnUrl)
        val link: String = routes.ClientsOverdueReturnsListController.onPageLoad(waypoints).url

        status(result) `mustBe` OK
        contentAsString(result) `mustBe` view(
          viewModel,
          link,
          currentReturnsNonEmpty = false,
          overdueReturnsNonEmpty = false
        )(request, messages(application)).toString
        verify(mockCurrentReturnsService, times(1)).getCurrentReturns(any())(any())
      }
    }

    "must return OK and the correct view for a GET when no current returns are retrieved" in {

      when(mockCurrentReturnsService.getCurrentReturns(any())(any())) thenReturn Seq.empty.toFuture

      val application = applicationBuilder(
        userAnswers = Some(emptyUserAnswers),
        registrationWrapper = registrationWrapper.copy(etmpDisplayRegistration = etmpDisplayRegistration)
      )
        .overrides(
          bind[CurrentReturnsService].toInstance(mockCurrentReturnsService)
        )
        .build()

      running(application) {
        implicit val msgs: Messages = messages(application)

        val request = FakeRequest(GET, clientsOutstandingReturnsRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ClientsOutstandingReturnsListView]

        val config = application.injector.instanceOf[FrontendAppConfig]

        val viewModel: ClientOutstandingReturnsListViewModel =
          ClientOutstandingReturnsListViewModel(Seq.empty, config.startCurrentReturnUrl)
        val link: String = routes.ClientsOverdueReturnsListController.onPageLoad(waypoints).url

        status(result) `mustBe` OK
        contentAsString(result) `mustBe` view(
          viewModel,
          link,
          currentReturnsNonEmpty = false,
          overdueReturnsNonEmpty = false
        )(request, messages(application)).toString
        verify(mockCurrentReturnsService, times(1)).getCurrentReturns(any())(any())
      }
    }
  }
}
