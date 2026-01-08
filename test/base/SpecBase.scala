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

package base

import controllers.actions.{FakeGetRegistrationAction, *}
import generators.Generators
import models.UserAnswers
import models.domain.VatCustomerInfo
import models.etmp.*
import org.scalacheck.Arbitrary
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{OptionValues, TryValues}
import pages.{EmptyWaypoints, Waypoints}
import play.api.Application
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.AnyContentAsEmpty
import play.api.test.CSRFTokenHelper.CSRFRequest
import play.api.test.FakeRequest
import uk.gov.hmrc.auth.core.{Enrolment, EnrolmentIdentifier, Enrolments}
import uk.gov.hmrc.domain.Vrn

import java.time.{Clock, Instant, LocalDate, ZoneId}
import java.util.UUID

trait SpecBase
  extends AnyFreeSpec
    with Matchers
    with TryValues
    with OptionValues
    with ScalaFutures
    with IntegrationPatience
    with Generators {

  val userAnswersId: String = "id"
  val intermediaryNumber = "IN9001234567"
  val intermediaryName = "Intermediary Company Name"
  val enrolmentKey = "HMRC-IOSS-INT"
  val identifierValue = "IM9001234569"
  val vrn: Vrn = Vrn("123456789")

  val intermediaryEnrolmentKey = "HMRC-IOSS-INT"
  val enrolments: Enrolments = Enrolments(Set(
    Enrolment(
      intermediaryEnrolmentKey,
      Seq(EnrolmentIdentifier("IntNumber", intermediaryNumber)),
      "test",
      None)
  ))

  val journeyId: String = UUID.randomUUID().toString

  val arbitraryInstant: Instant = arbitraryDate.arbitrary.sample.value.atStartOfDay(ZoneId.systemDefault()).toInstant
  val stubClockAtArbitraryDate: Clock = Clock.fixed(arbitraryInstant, ZoneId.systemDefault())
  
  val waypoints: Waypoints = EmptyWaypoints

  lazy val fakeRequest: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest("", "/endpoint").withCSRFToken.asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]

  def testEnrolments: Enrolments = Enrolments(Set(Enrolment("HMRC-MTD-VAT", Seq(EnrolmentIdentifier("VRN", vrn.vrn)), "Activated")))
  
  def emptyUserAnswers : UserAnswers = UserAnswers(userAnswersId)

  def messages(app: Application): Messages = app.injector.instanceOf[MessagesApi].preferred(FakeRequest())

  val vatCustomerInfo: VatCustomerInfo = {
    VatCustomerInfo(
      registrationDate = LocalDate.now(stubClockAtArbitraryDate),
      desAddress = arbitraryDesAddress.arbitrary.sample.value,
      organisationName = Some("Company name"),
      individualName = None,
      singleMarketIndicator = true,
      deregistrationDecisionDate = None
    )
  }

  val registrationWrapper: RegistrationWrapper = {
    val etmpDisplayRegistration = Arbitrary.arbitrary[EtmpDisplayRegistration].sample.value
    RegistrationWrapper(vatCustomerInfo, etmpDisplayRegistration = etmpDisplayRegistration)
  }

  def emptyUserAnswersWithVatInfo: UserAnswers = emptyUserAnswers.copy(vatInfo = Some(vatCustomerInfo))

  protected def applicationBuilder(
                                    userAnswers: Option[UserAnswers] = None,
                                    clock: Option[Clock] = None,
                                    registrationWrapper: RegistrationWrapper = registrationWrapper,
                                    getRegistrationAction: Option[GetRegistrationAction] = None
                                  ): GuiceApplicationBuilder = {

    val clockToBind = clock.getOrElse(stubClockAtArbitraryDate)
    val getRegistrationActionBind = if (getRegistrationAction.nonEmpty) {
      bind[GetRegistrationAction].toInstance(getRegistrationAction.get)
    } else {
      bind[GetRegistrationAction].toInstance(new FakeGetRegistrationAction(registrationWrapper))
    }

    new GuiceApplicationBuilder()
      .overrides(
        bind[DataRequiredAction].to[DataRequiredActionImpl],
        bind[IdentifierAction].to[FakeIdentifierAction],
        bind[DataRetrievalAction].toInstance(new FakeDataRetrievalAction(userAnswers)),
        bind[CheckBouncedEmailFilterProvider].toInstance(new FakeCheckBouncedEmailFilterProvider()),
        getRegistrationActionBind,
        bind[Clock].toInstance(clockToBind),
      )
  }
}
