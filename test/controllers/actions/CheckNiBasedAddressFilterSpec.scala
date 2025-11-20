package controllers.actions

import base.SpecBase
import config.FrontendAppConfig
import models.etmp.EtmpExclusionReason.TransferringMSID
import models.etmp.{EtmpExclusion, EtmpOtherAddress}
import models.DesAddress
import models.requests.RegistrationRequest
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.Result
import play.api.mvc.Results.Redirect
import play.api.test.FakeRequest
import play.api.test.Helpers.running

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CheckNiBasedAddressFilterSpec extends SpecBase with MockitoSugar {

  class Harness(frontendAppConfig: FrontendAppConfig) extends CheckNiBasedAddressFilterImpl(frontendAppConfig) {
    def callFilter(request: RegistrationRequest[_]): Future[Option[Result]] = filter(request)
  }

  private val nonNiVatInfo = vatCustomerInfo.copy(
    desAddress = DesAddress(
      line1 = "1 The Street",
      line2 = None,
      line3 = None,
      line4 = None,
      line5 = None,
      postCode = Some("AA11 1AA"),
      countryCode = "GB"
    )
  )

  private val niVatInfo = vatCustomerInfo.copy(
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

  ".filter" - {

    "must return None" - {

      "when an intermediary is excluded" in {

        val excludedIntermediary = registrationWrapper.copy(
          etmpDisplayRegistration = arbitraryEtmpDisplayRegistration.arbitrary.sample.value.copy(
            exclusions = Seq(
              EtmpExclusion(
                exclusionReason = TransferringMSID,
                effectiveDate = LocalDate.of(2025, 1, 1),
                decisionDate = LocalDate.of(2025, 1, 1),
                quarantine = false
              )
            )
          )
        )

        val application = applicationBuilder(None).build()

        running(application) {

          val request = RegistrationRequest(
            FakeRequest(),
            userId = userAnswersId,
            enrolments = enrolments,
            vrn = vrn,
            intermediaryNumber = intermediaryNumber,
            registrationWrapper = excludedIntermediary
          )

          val frontendAppConfig = application.injector.instanceOf[FrontendAppConfig]
          val controller = new Harness(frontendAppConfig)

          val result = controller.callFilter(request).futureValue

          result mustBe None
        }
      }

      "when VAT address is NI and otherAddress field is empty" in {

        val niVatWithEmptyOtherAddress = registrationWrapper.copy(
          vatInfo = niVatInfo,
          etmpDisplayRegistration = arbitraryEtmpDisplayRegistration.arbitrary.sample.value.copy(
            otherAddress = None
          )
        )

        val request = RegistrationRequest(
          FakeRequest(),
          userId = userAnswersId,
          enrolments = enrolments,
          vrn = vrn,
          intermediaryNumber = intermediaryNumber,
          registrationWrapper = niVatWithEmptyOtherAddress
        )

        val application = applicationBuilder(None).build()

        running(application) {

          val frontendAppConfig = application.injector.instanceOf[FrontendAppConfig]
          val controller = new Harness(frontendAppConfig)

          val result = controller.callFilter(request).futureValue

          result mustBe None
        }
      }

      "when both VAT address and otherAddress is NI based" in {

        val emptyOtherAddress = registrationWrapper.copy(
          vatInfo = niVatInfo,
          etmpDisplayRegistration = arbitraryEtmpDisplayRegistration.arbitrary.sample.value.copy(
            otherAddress = Some(
              EtmpOtherAddress(
                issuedBy = "GB",
                tradingName = Some("Company name"),
                addressLine1 = "Other Address Line 1",
                addressLine2 = Some("Other Address Line 2"),
                townOrCity = "Other Town or City",
                regionOrState = Some("Other Region or State"),
                postcode = "BT11AH"
              )
            )
          )
        )

        val application = applicationBuilder(None).build()

        running(application) {

          val request = RegistrationRequest(
            FakeRequest(),
            userId = userAnswersId,
            enrolments = enrolments,
            vrn = vrn,
            intermediaryNumber = intermediaryNumber,
            registrationWrapper = emptyOtherAddress
          )

          val frontendAppConfig = application.injector.instanceOf[FrontendAppConfig]
          val controller = new Harness(frontendAppConfig)

          val result = controller.callFilter(request).futureValue

          result mustBe None
        }
      }

      "when intermediary is excluded, VAT address si NI and otherAddress field is empty" in {

        val emptyOtherAddress = registrationWrapper.copy(
          vatInfo = niVatInfo,
          etmpDisplayRegistration = arbitraryEtmpDisplayRegistration.arbitrary.sample.value.copy(
            exclusions = Seq(
              EtmpExclusion(
                exclusionReason = TransferringMSID,
                effectiveDate = LocalDate.of(2025, 1, 1),
                decisionDate = LocalDate.of(2025, 1, 1),
                quarantine = false
              )
            ),
            otherAddress = None
          )
        )

        val application = applicationBuilder(None).build()

        running(application) {

          val request = RegistrationRequest(
            FakeRequest(),
            userId = userAnswersId,
            enrolments = enrolments,
            vrn = vrn,
            intermediaryNumber = intermediaryNumber,
            registrationWrapper = emptyOtherAddress
          )

          val frontendAppConfig = application.injector.instanceOf[FrontendAppConfig]
          val controller = new Harness(frontendAppConfig)

          val result = controller.callFilter(request).futureValue

          result mustBe None
        }
      }

      "when intermediary is excluded, and both VAT address and otherAddress is NI based" in {

        val emptyOtherAddress = registrationWrapper.copy(
          vatInfo = niVatInfo,
          etmpDisplayRegistration = arbitraryEtmpDisplayRegistration.arbitrary.sample.value.copy(
            exclusions = Seq(
              EtmpExclusion(
                exclusionReason = TransferringMSID,
                effectiveDate = LocalDate.of(2025, 1, 1),
                decisionDate = LocalDate.of(2025, 1, 1),
                quarantine = false
              )
            ),
            otherAddress = Some(
              EtmpOtherAddress(
                issuedBy = "GB",
                tradingName = Some("Company name"),
                addressLine1 = "Other Address Line 1",
                addressLine2 = Some("Other Address Line 2"),
                townOrCity = "Other Town or City",
                regionOrState = Some("Other Region or State"),
                postcode = "BT11AH"
              )
            )
          )
        )

        val application = applicationBuilder(None).build()

        running(application) {

          val request = RegistrationRequest(
            FakeRequest(),
            userId = userAnswersId,
            enrolments = enrolments,
            vrn = vrn,
            intermediaryNumber = intermediaryNumber,
            registrationWrapper = emptyOtherAddress
          )

          val frontendAppConfig = application.injector.instanceOf[FrontendAppConfig]
          val controller = new Harness(frontendAppConfig)

          val result = controller.callFilter(request).futureValue

          result mustBe None
        }
      }
    }

    "must redirect users to the intermediary frontend service so they can provide their latest information" - {

      "when VAT address is non-NI and the otherAddress field is empty" in {

        val emptyOtherAddressWithNonNiVat = registrationWrapper.copy(
          vatInfo = nonNiVatInfo,
          etmpDisplayRegistration = arbitraryEtmpDisplayRegistration.arbitrary.sample.value.copy(
            exclusions = Seq.empty,
            otherAddress = None
          )
        )

        val application = applicationBuilder(None).build()

        running(application) {

          val request = RegistrationRequest(
            FakeRequest(),
            userId = userAnswersId,
            enrolments = enrolments,
            vrn = vrn,
            intermediaryNumber = intermediaryNumber,
            registrationWrapper = emptyOtherAddressWithNonNiVat
          )

          val frontendAppConfig = application.injector.instanceOf[FrontendAppConfig]
          val controller = new Harness(frontendAppConfig)

          val result = controller.callFilter(request).futureValue

          result.value mustBe Redirect(frontendAppConfig.changeYourRegistrationUrl)
        }
      }

      "when both VAT address and otherAddress is non-NI" in {

        val emptyOtherAddressWithNonNiVat = registrationWrapper.copy(
          vatInfo = nonNiVatInfo,
          etmpDisplayRegistration = arbitraryEtmpDisplayRegistration.arbitrary.sample.value.copy(
            exclusions = Seq.empty,
            otherAddress = Some(
              EtmpOtherAddress(
                issuedBy = "GB",
                tradingName = Some("Company name"),
                addressLine1 = "Other Address Line 1",
                addressLine2 = Some("Other Address Line 2"),
                townOrCity = "Other Town or City",
                regionOrState = Some("Other Region or State"),
                postcode = "AA11AH"
              )
            )
          )
        )

        val application = applicationBuilder(None).build()

        running(application) {

          val request = RegistrationRequest(
            FakeRequest(),
            userId = userAnswersId,
            enrolments = enrolments,
            vrn = vrn,
            intermediaryNumber = intermediaryNumber,
            registrationWrapper = emptyOtherAddressWithNonNiVat
          )

          val frontendAppConfig = application.injector.instanceOf[FrontendAppConfig]
          val controller = new Harness(frontendAppConfig)

          val result = controller.callFilter(request).futureValue

          result.value mustBe Redirect(frontendAppConfig.changeYourRegistrationUrl)
        }
      }

      "when intermediary is not excluded" in {

        val notExcludedIntermediary = registrationWrapper.copy(
          etmpDisplayRegistration = arbitraryEtmpDisplayRegistration.arbitrary.sample.value.copy(
            exclusions = Seq.empty
          )
        )

        val application = applicationBuilder(None).build()

        running(application) {

          val request = RegistrationRequest(
            FakeRequest(),
            userId = userAnswersId,
            enrolments = enrolments,
            vrn = vrn,
            intermediaryNumber = intermediaryNumber,
            registrationWrapper = notExcludedIntermediary
          )

          val frontendAppConfig = application.injector.instanceOf[FrontendAppConfig]
          val controller = new Harness(frontendAppConfig)

          val result = controller.callFilter(request).futureValue

          result.value mustBe Redirect(frontendAppConfig.changeYourRegistrationUrl)
        }
      }
    }
  }
}
