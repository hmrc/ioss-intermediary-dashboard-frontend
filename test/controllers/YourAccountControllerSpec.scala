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
import connectors.{RegistrationConnector, SecureMessageConnector}
import models.DesAddress
import models.domain.VatCustomerInfo
import models.etmp.EtmpExclusionReason.TransferringMSID
import models.etmp.{EtmpDisplayRegistration, EtmpExclusion, RegistrationWrapper}
import models.responses.InternalServerError
import models.securemessage.responses.{SecureMessageCount, SecureMessageResponse, SecureMessageResponseWithCount, TaxpayerName}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.mockito.MockitoSugar
import pages.{EmptyWaypoints, Waypoints}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.YourAccountView
import utils.FutureSyntax.FutureOps
import viewmodels.dashboard.DashboardUrlsViewModel

import java.time.LocalDate

class YourAccountControllerSpec extends SpecBase with MockitoSugar {

  private val waypoints: Waypoints = EmptyWaypoints
  private val businessName = "Company name"
  private val intermediaryNumber = "IN9001234567"

  val emptyTaxpayerName = TaxpayerName(
    title = None,
    forename = None,
    secondForename = None,
    surname = None,
    honours = None,
    line1 = None,
    line2 = None,
    line3 = None
  )

  val testSecureMessageResponse = SecureMessageResponse(
    messageType = "messageType",
    id = "secureId",
    subject = "subject",
    issueDate = "2025-10-06",
    senderName = "senderName",
    unreadMessages = true,
    count = 3,
    taxpayerName = Some(emptyTaxpayerName),
    validFrom = "2025-10-06",
    sentInError = false,
    language = Some("en")
  )

  val testSecureMessageCount = SecureMessageCount(total = 3, unread = 3)

  val secureMessageResponseWithCount = SecureMessageResponseWithCount(
    items = Seq(testSecureMessageResponse),
    count = testSecureMessageCount
  )

  private val etmpDisplayRegistration: EtmpDisplayRegistration = arbitraryEtmpDisplayRegistration.arbitrary.sample.value

  private val registrationWrapper: RegistrationWrapper = RegistrationWrapper(
    vatInfo = vatCustomerInfo,
    etmpDisplayRegistration = etmpDisplayRegistration
  )


  val mockSecureMessageConnector: SecureMessageConnector = mock[SecureMessageConnector]

  lazy val yourAccountRoute: String = routes.YourAccountController.onPageLoad(waypoints).url

  "YourAccount Controller" - {

    "should display your account view" - {

      "must return OK and the correct view for a GET" in {

        val niVatInfo = vatCustomerInfo.copy(
          desAddress = DesAddress(
            line1 = "1 The Street",
            line2 = None,
            line3 = None,
            line4 = None,
            line5 = None,
            postCode = Some("BT11 1AA"),
            countryCode = "GB"
          )
        )

        val registrationWrapperEmptyExclusionsAndEmptyOtherAddress = registrationWrapper.copy(
          vatInfo = niVatInfo,
          etmpDisplayRegistration = arbitraryEtmpDisplayRegistration.arbitrary.sample.value.copy(
            exclusions = Seq.empty,
            otherAddress = None
          )
        )

        val mockRegistrationConnector = mock[RegistrationConnector]

        when(mockRegistrationConnector.getNumberOfPendingRegistrations(any())(any()))
          .thenReturn(1.toLong.toFuture)
        when(mockRegistrationConnector.getNumberOfSavedUserAnswers(any())(any()))
          .thenReturn(1.toLong.toFuture)
        when(mockRegistrationConnector.getVatCustomerInfo(any())(any()))
          .thenReturn(Right(vatCustomerInfo).toFuture)
        when(mockSecureMessageConnector.getMessages(any(), any(), any(), any(), any())(any()))
          .thenReturn(Right(secureMessageResponseWithCount).toFuture)
        when(mockRegistrationConnector.displayRegistration(any())(any()))
          .thenReturn(Right(registrationWrapperEmptyExclusionsAndEmptyOtherAddress).toFuture)

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), registrationWrapper = registrationWrapperEmptyExclusionsAndEmptyOtherAddress)
          .overrides(bind[RegistrationConnector].toInstance(mockRegistrationConnector))
          .overrides(bind[SecureMessageConnector].toInstance(mockSecureMessageConnector))
          .build()
        val appConfig = application.injector.instanceOf[FrontendAppConfig]

        val urls = DashboardUrlsViewModel(
          addClientUrl = appConfig.addClientUrl,
          viewClientReturnsListUrl = controllers.routes.ClientReturnsListController.onPageLoad().url,
          viewClientsListUrl = controllers.routes.ClientListController.onPageLoad().url,
          changeYourRegistrationUrl = appConfig.changeYourRegistrationUrl,
          pendingClientsUrl = controllers.routes.ClientAwaitingActivationController.onPageLoad().url,
          secureMessagesUrl = controllers.routes.SecureMessagesController.onPageLoad().url,
          leaveThisServiceUrl = Some(appConfig.leaveThisServiceUrl),
          continueSavedRegUrl = appConfig.continueRegistrationUrl,
          rejoinSchemeUrl = appConfig.rejoinSchemeUrl
        )

        running(application) {
          val request = FakeRequest(GET, yourAccountRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[YourAccountView]

          status(result) `mustEqual` OK
          contentAsString(result) mustEqual view(
            waypoints,
            businessName,
            intermediaryNumber,
            numberOfMessages = secureMessageResponseWithCount.count.total.toInt,
            true,
            1,
            cancelYourRequestToLeaveUrl = None,
            1,
            urls,
            false
          )(request, messages(application)).toString
        }
      }

      "must return OK with cancelYourRequestToLeave link and without leaveThisService link when a trader is excluded" in {

        val mockRegistrationConnector = mock[RegistrationConnector]

        val registrationWrapper: RegistrationWrapper = arbitrary[RegistrationWrapper].sample.value

        val exclusion = EtmpExclusion(
          TransferringMSID,
          LocalDate.now(stubClockAtArbitraryDate).plusDays(2),
          LocalDate.now(stubClockAtArbitraryDate).minusDays(1),
          quarantine = false
        )

        val registrationWrapperEmptyExclusions: RegistrationWrapper =
          registrationWrapper
            .copy(vatInfo = registrationWrapper.vatInfo)
            .copy(etmpDisplayRegistration = registrationWrapper.etmpDisplayRegistration.copy(exclusions = Seq(exclusion)))

        when(mockRegistrationConnector.getNumberOfPendingRegistrations(any())(any()))
          .thenReturn(1.toLong.toFuture)
        when(mockRegistrationConnector.getNumberOfSavedUserAnswers(any())(any()))
          .thenReturn(1.toLong.toFuture)
        when(mockRegistrationConnector.getVatCustomerInfo(any())(any()))
          .thenReturn(Right(vatCustomerInfo).toFuture)
        when(mockSecureMessageConnector.getMessages(any(), any(), any(), any(), any())(any()))
          .thenReturn(Right(secureMessageResponseWithCount).toFuture)
        when(mockRegistrationConnector.displayRegistration(any())(any()))
          .thenReturn(Right(registrationWrapperEmptyExclusions).toFuture)

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), registrationWrapper = registrationWrapperEmptyExclusions)
          .overrides(bind[RegistrationConnector].toInstance(mockRegistrationConnector))
          .overrides(bind[SecureMessageConnector].toInstance(mockSecureMessageConnector))
          .build()
        val appConfig = application.injector.instanceOf[FrontendAppConfig]

        val urls = DashboardUrlsViewModel(
          addClientUrl = appConfig.addClientUrl,
          viewClientReturnsListUrl = controllers.routes.ClientReturnsListController.onPageLoad().url,
          viewClientsListUrl = controllers.routes.ClientListController.onPageLoad().url,
          changeYourRegistrationUrl = appConfig.changeYourRegistrationUrl,
          pendingClientsUrl = controllers.routes.ClientAwaitingActivationController.onPageLoad().url,
          secureMessagesUrl = controllers.routes.SecureMessagesController.onPageLoad().url,
          leaveThisServiceUrl = None,
          continueSavedRegUrl = appConfig.continueRegistrationUrl,
          rejoinSchemeUrl = appConfig.rejoinSchemeUrl
        )


        running(application) {
          val request = FakeRequest(GET, yourAccountRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[YourAccountView]

          status(result) `mustEqual` OK
          contentAsString(result) mustEqual view(
            waypoints,
            businessName,
            intermediaryNumber,
            numberOfMessages = secureMessageResponseWithCount.count.total.toInt,
            true,
            1,
            cancelYourRequestToLeaveUrl = Some(appConfig.cancelYourRequestToLeaveUrl),
            1,
            urls,
            false
          )(request, messages(application)).toString
        }
      }
    }

    "must throw an exception and log the error when an unexpected error is returned" in {

      val mockRegistrationConnector = mock[RegistrationConnector]

      when(mockRegistrationConnector.getNumberOfPendingRegistrations(any())(any()))
        .thenReturn(0.toLong.toFuture)
      when(mockRegistrationConnector.getNumberOfSavedUserAnswers(any())(any()))
        .thenReturn(1.toLong.toFuture)
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
