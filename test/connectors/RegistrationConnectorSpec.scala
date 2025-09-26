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

package connectors

import base.SpecBase
import com.github.tomakehurst.wiremock.client.WireMock.*
import models.SavedPendingRegistration
import models.domain.VatCustomerInfo
import models.etmp.EtmpClientDetails
import models.responses.*
import org.scalacheck.Gen
import play.api.Application
import play.api.http.Status.*
import play.api.libs.json.Json
import play.api.test.Helpers.running
import testutils.WireMockHelper
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}

class RegistrationConnectorSpec extends SpecBase with WireMockHelper {

  implicit private lazy val hc: HeaderCarrier = HeaderCarrier()

  private val vatNumber: String = "123456789"

  private val etmpClientDetails: Seq[EtmpClientDetails] = Gen.listOfN(3, arbitraryEtmpClientDetails.arbitrary).sample.value

  private def dashboardApplication: Application = applicationBuilder()
    .configure(
      "microservice.services.ioss-intermediary-dashboard.port" -> server.port,
      "microservice.services.ioss-intermediary-registration.port" -> server.port
    )
    .build()

  private def netpRegistrationApplication: Application = applicationBuilder()
    .configure(
      "microservice.services.ioss-netp-registration.port" -> server.port
    )
    .build()

  "RegistrationConnector" - {

    ".getCustomerVatInfo" - {

      val url: String = "/ioss-intermediary-dashboard/vat-information/123456789"

      "must return vat information when the backend returns some" in {

        running(dashboardApplication) {
          val connector: RegistrationConnector = dashboardApplication.injector.instanceOf[RegistrationConnector]

          val vatInfo: VatCustomerInfo = vatCustomerInfo

          val responseBody = Json.toJson(vatInfo).toString()

          server.stubFor(get(urlEqualTo(url)).willReturn(ok().withBody(responseBody)))

          val result = connector.getVatCustomerInfo(vatNumber).futureValue

          result `mustBe` Right(vatInfo)
        }
      }

      "must return invalid json when the backend returns some" in {

        running(dashboardApplication) {
          val connector: RegistrationConnector = dashboardApplication.injector.instanceOf[RegistrationConnector]

          val responseBody = Json.obj("test" -> "test").toString()

          server.stubFor(get(urlEqualTo(url)).willReturn(ok().withBody(responseBody)))

          val result = connector.getVatCustomerInfo(vatNumber).futureValue

          result `mustBe` Left(InvalidJson)
        }
      }

      "must return Left(NotFound) when the backend returns NOT_FOUND" in {

        running(dashboardApplication) {
          val connector: RegistrationConnector = dashboardApplication.injector.instanceOf[RegistrationConnector]

          server.stubFor(get(urlEqualTo(url)).willReturn(notFound()))

          val result = connector.getVatCustomerInfo(vatNumber).futureValue

          result `mustBe` Left(VatCustomerNotFound)
        }
      }

      "must return Left(UnexpectedResponseStatus) when the backend returns another error code" in {

        val status = Gen.oneOf(BAD_REQUEST, INTERNAL_SERVER_ERROR, NOT_IMPLEMENTED, BAD_GATEWAY, SERVICE_UNAVAILABLE).sample.value

        running(dashboardApplication) {
          val connector: RegistrationConnector = dashboardApplication.injector.instanceOf[RegistrationConnector]

          server.stubFor(get(urlEqualTo(url)).willReturn(aResponse().withStatus(status)))

          val result = connector.getVatCustomerInfo(vatNumber).futureValue

          result `mustBe` Left(UnexpectedResponseStatus(status, s"Received unexpected response code $status"))
        }
      }
    }

    ".getDisplayRegistration" - {

      val getDisplayRegistrationUrl: String = s"/ioss-intermediary-registration/get-registration/$intermediaryNumber"

      "must return Right(ETMP Client Details) when the server returns a successful response and JSON is parsed correctly" in {

        val clientDetailsJson = Json.toJson(etmpClientDetails).toString

        val json =
          s"""{
             |  "etmpDisplayRegistration": {
             |    "clientDetails": $clientDetailsJson
             |  }
             |}""".stripMargin

        val connector = application.injector.instanceOf[RegistrationConnector]

        server.stubFor(
          get(urlEqualTo(getDisplayRegistrationUrl))
            .willReturn(ok(json))
        )

        running(application) {

          val result = connector.getDisplayRegistration(intermediaryNumber).futureValue

          result `mustBe` Right(etmpClientDetails)
        }
      }

      "must return Left(InvalidJson) when when JSON is not parsed correctly" in {

        val invalidJson =
          s"""{
             |  "etmpDisplayRegistration": {
             |    "clientDetails": "1234"
             |  }
             |}""".stripMargin

        val connector = application.injector.instanceOf[RegistrationConnector]

        server.stubFor(
          get(urlEqualTo(getDisplayRegistrationUrl))
            .willReturn(ok(invalidJson)
            )
        )

        running(application) {

          val result = connector.getDisplayRegistration(intermediaryNumber).futureValue

          result `mustBe` Left(InvalidJson)
        }
      }

      "must return Left(InternalServerError) when server responds with an error" in {

        val app = application

        server.stubFor(
          get(urlEqualTo(getDisplayRegistrationUrl))
            .willReturn(serverError()
            )
        )

        running(application) {

          val connector = app.injector.instanceOf[RegistrationConnector]

          val result = connector.getDisplayRegistration(intermediaryNumber: String).futureValue

          result `mustBe` Left(InternalServerError)
        }
      }
    }

    "getNumberOfPendingRegistrations" - {

      val netpPendingRegCountUrl: String = s"/ioss-netp-registration/pending-registrations/count/$intermediaryNumber"


      "must return a count when the backend returns a count" in {

        running(netpRegistrationApplication) {
          val connector: RegistrationConnector = netpRegistrationApplication.injector.instanceOf[RegistrationConnector]

          val responseBody = 1

          server.stubFor(get(urlEqualTo(netpPendingRegCountUrl))
            .willReturn(aResponse().withBody(responseBody.toString)))

          val result = connector.getNumberOfPendingRegistrations(intermediaryNumber).futureValue

          result `mustBe` 1
        }
      }

      "must return Left(UnexpectedResponseStatus) when the backend returns another error code" in {

        val status = Gen.oneOf(BAD_REQUEST, INTERNAL_SERVER_ERROR, NOT_IMPLEMENTED, BAD_GATEWAY, SERVICE_UNAVAILABLE).sample.value

        running(netpRegistrationApplication) {
          val connector: RegistrationConnector = netpRegistrationApplication.injector.instanceOf[RegistrationConnector]

          server.stubFor(get(urlEqualTo(netpPendingRegCountUrl)).willReturn(aResponse().withStatus(status)))

          val result = connector.getNumberOfPendingRegistrations(intermediaryNumber)

          whenReady(result.failed) {
            case e: UpstreamErrorResponse =>
              e.statusCode mustBe status
            case other =>
              fail(s"Expected UpstreamErrorResponse but got $other")
          }
        }
      }
    }

    ".getPendingRegistrations" - {

      val netpPendingRegUrl: String = s"/ioss-netp-registration/pending-registrations/$intermediaryNumber"

      val arbSavedPendingReg = arbitrarySavedPendingRegistration.arbitrary.sample.value
      "must return vat information when the backend returns some" in {

        running(netpRegistrationApplication) {
          val connector: RegistrationConnector = netpRegistrationApplication.injector.instanceOf[RegistrationConnector]

          val response: Seq[SavedPendingRegistration] = Seq(arbSavedPendingReg, arbSavedPendingReg, arbSavedPendingReg)

          val responseBody = Json.toJson(response).toString()

          server.stubFor(get(urlEqualTo(netpPendingRegUrl)).willReturn(ok().withBody(responseBody)))

          val result = connector.getPendingRegistrations(intermediaryNumber).futureValue

          result `mustBe` Right(response)
        }
      }

      "must return invalid json when the backend returns some" in {

        running(netpRegistrationApplication) {
          val connector: RegistrationConnector = netpRegistrationApplication.injector.instanceOf[RegistrationConnector]

          val responseBody = Json.obj("test" -> "test").toString()

          server.stubFor(get(urlEqualTo(netpPendingRegUrl)).willReturn(ok().withBody(responseBody)))

          val result = connector.getPendingRegistrations(intermediaryNumber).futureValue

          result `mustBe` Left(InvalidJson)
        }
      }

      "must return Left(NotFound) when the backend returns NOT_FOUND" in {

        running(netpRegistrationApplication) {
          val connector: RegistrationConnector = netpRegistrationApplication.injector.instanceOf[RegistrationConnector]

          server.stubFor(get(urlEqualTo(netpPendingRegUrl)).willReturn(notFound()))

          val result = connector.getPendingRegistrations(intermediaryNumber).futureValue

          result `mustBe` Left(UnexpectedResponseStatus(NOT_FOUND, "Unexpected response when trying to retrieve the pending registration, status 404 returned"))
        }
      }

      "must return Left(UnexpectedResponseStatus) when the backend returns another error code" in {

        val status = Gen.oneOf(BAD_REQUEST, INTERNAL_SERVER_ERROR, NOT_IMPLEMENTED, BAD_GATEWAY, SERVICE_UNAVAILABLE).sample.value

        running(netpRegistrationApplication) {
          val connector: RegistrationConnector = netpRegistrationApplication.injector.instanceOf[RegistrationConnector]

          server.stubFor(get(urlEqualTo(netpPendingRegUrl)).willReturn(aResponse().withStatus(status)))

          val result = connector.getPendingRegistrations(intermediaryNumber).futureValue

          result `mustBe` Left(UnexpectedResponseStatus(status, s"Unexpected response when trying to retrieve the pending registration, status $status returned"))
        }
      }
    }

    "getNumberOfSavedUserAnswers" - {

      val netpSavedCountUrl: String = s"/ioss-netp-registration/save-for-later/count/$intermediaryNumber"


      "must return a count when the backend returns a count" in {

        running(netpRegistrationApplication) {
          val connector: RegistrationConnector = netpRegistrationApplication.injector.instanceOf[RegistrationConnector]

          val responseBody = 1

          server.stubFor(get(urlEqualTo(netpSavedCountUrl))
            .willReturn(aResponse().withBody(responseBody.toString)))

          val result = connector.getNumberOfSavedUserAnswers(intermediaryNumber).futureValue

          result `mustBe` 1
        }
      }

      "must return Left(UnexpectedResponseStatus) when the backend returns another error code" in {

        val status = Gen.oneOf(BAD_REQUEST, INTERNAL_SERVER_ERROR, NOT_IMPLEMENTED, BAD_GATEWAY, SERVICE_UNAVAILABLE).sample.value

        running(netpRegistrationApplication) {
          val connector: RegistrationConnector = netpRegistrationApplication.injector.instanceOf[RegistrationConnector]

          server.stubFor(get(urlEqualTo(netpSavedCountUrl)).willReturn(aResponse().withStatus(status)))

          val result = connector.getNumberOfSavedUserAnswers(intermediaryNumber)

          whenReady(result.failed) {
            case e: UpstreamErrorResponse =>
              e.statusCode mustBe status
            case other =>
              fail(s"Expected UpstreamErrorResponse but got $other")
          }
        }
      }
    }


  }
}
