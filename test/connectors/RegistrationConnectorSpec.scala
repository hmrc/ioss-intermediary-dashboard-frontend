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
import models.domain.VatCustomerInfo
import models.responses.*
import org.scalacheck.Gen
import play.api.Application
import play.api.http.Status.*
import play.api.libs.json.Json
import play.api.test.Helpers.running
import testutils.WireMockHelper
import uk.gov.hmrc.http.HeaderCarrier

class RegistrationConnectorSpec extends SpecBase with WireMockHelper {

  implicit private lazy val hc: HeaderCarrier = HeaderCarrier()

  private val vatNumber = "123456789"

  private def application: Application = applicationBuilder()
    .configure(
      "microservice.services.ioss-intermediary-dashboard.port" -> server.port
    )
    .build()

  "RegistrationConnector" - {

    ".getCustomerVatInfo" - {

      val url: String = "/ioss-intermediary-dashboard/vat-information/123456789"

      "must return vat information when the backend returns some" in {

        running(application) {
          val connector: RegistrationConnector = application.injector.instanceOf[RegistrationConnector]

          val vatInfo: VatCustomerInfo = vatCustomerInfo

          val responseBody = Json.toJson(vatInfo).toString()

          server.stubFor(get(urlEqualTo(url)).willReturn(ok().withBody(responseBody)))

          val result = connector.getVatCustomerInfo(vatNumber).futureValue

          result `mustBe` Right(vatInfo)
        }
      }

      "must return invalid json when the backend returns some" in {

        running(application) {
          val connector: RegistrationConnector = application.injector.instanceOf[RegistrationConnector]

          val responseBody = Json.obj("test" -> "test").toString()

          server.stubFor(get(urlEqualTo(url)).willReturn(ok().withBody(responseBody)))

          val result = connector.getVatCustomerInfo(vatNumber).futureValue

          result `mustBe` Left(InvalidJson)
        }
      }

      "must return Left(NotFound) when the backend returns NOT_FOUND" in {

        running(application) {
          val connector: RegistrationConnector = application.injector.instanceOf[RegistrationConnector]

          server.stubFor(get(urlEqualTo(url)).willReturn(notFound()))

          val result = connector.getVatCustomerInfo(vatNumber).futureValue

          result `mustBe` Left(VatCustomerNotFound)
        }
      }

      "must return Left(UnexpectedResponseStatus) when the backend returns another error code" in {

        val status = Gen.oneOf(BAD_REQUEST, INTERNAL_SERVER_ERROR, NOT_IMPLEMENTED, BAD_GATEWAY, SERVICE_UNAVAILABLE).sample.value

        running(application) {
          val connector: RegistrationConnector = application.injector.instanceOf[RegistrationConnector]

          server.stubFor(get(urlEqualTo(url)).willReturn(aResponse().withStatus(status)))

          val result = connector.getVatCustomerInfo(vatNumber).futureValue

          result `mustBe` Left(UnexpectedResponseStatus(status, s"Received unexpected response code $status"))
        }
      }
    }
  }
}
