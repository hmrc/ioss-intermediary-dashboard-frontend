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

package connectors.test

import base.SpecBase
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, post, postRequestedFor, urlEqualTo}
import play.api.Application
import play.api.http.Status.{BAD_REQUEST, CREATED, INTERNAL_SERVER_ERROR}
import play.api.libs.json.Json
import play.api.test.Helpers.running
import testutils.WireMockHelper
import uk.gov.hmrc.http.HeaderCarrier

import scala.jdk.CollectionConverters.*


class TestOnlySecureMessagingConnectorSpec extends SpecBase with WireMockHelper {

  implicit private lazy val hc: HeaderCarrier = HeaderCarrier()
  private val url = "/secure-messaging/v4/message"

  private def application: Application = applicationBuilder()
    .configure(
      "microservice.services.secure-message.port" -> server.port(),
    )
    .build()

  "TestOnlySecureMessagingConnector" - {

    ".createMessage" - {

      "must POST to secure-messaging and return 201" in {
        running(application) {
          val connector = application.injector.instanceOf[TestOnlySecureMessagingConnector]

          server.stubFor(
            post(urlEqualTo(url))
              .willReturn(aResponse()
                .withStatus(CREATED))
          )

          val result = connector.createMessage(enrolmentKey, identifierValue).futureValue

          result.status mustBe CREATED
        }
      }

      "must return error when backend returns non-201" in {
        running(application) {
          val connector = application.injector.instanceOf[TestOnlySecureMessagingConnector]

          server.stubFor(
            post(urlEqualTo(url))
              .willReturn(aResponse().withStatus(INTERNAL_SERVER_ERROR))
          )

          val result = connector.createMessage(enrolmentKey, identifierValue).futureValue

          result.status mustBe INTERNAL_SERVER_ERROR
        }
      }

      "must return a message with unique id and email body id" in {
        running(application) {
          val connector = application.injector.instanceOf[TestOnlySecureMessagingConnector]

          server.stubFor(
            post(urlEqualTo(url))
              .willReturn(aResponse().withStatus(CREATED))
          )

          connector.createMessage(enrolmentKey, identifierValue).futureValue
          connector.createMessage(enrolmentKey, identifierValue).futureValue

          val requests = server.findAll(postRequestedFor(urlEqualTo(url))).asScala

          requests.size mustBe 2

          val ids = requests.map { req =>
            val json = Json.parse(req.getBodyAsString)
            val extId = (json \ "externalRef" \ "id").as[String]
            val bodyId = (json \ "content")(0).\("body").as[String]
            (extId, bodyId)
          }

          ids.map(_._1).distinct.size mustBe 2
          ids.map(_._2).distinct.size mustBe 2
        }
      }

    }

    ".createCustomMessage" - {

      "must POST to secure-messaging and return 201" in {
        running(application) {
          val connector = application.injector.instanceOf[TestOnlySecureMessagingConnector]

          val expectedBody = Json.obj("result" -> "created").toString()

          server.stubFor(
            post(urlEqualTo(url))
              .willReturn(
                aResponse()
                  .withStatus(CREATED)
                  .withBody(expectedBody)
              )
          )

          val result = connector.createCustomMessage(
            enrolmentKey, identifierValue, "firstName", "lastName", "test@mail.com", "subject", "body"
          ).futureValue

          result.status mustBe CREATED
          result.body mustBe expectedBody
        }
      }

      "must return error when backend returns non-201" in {
        running(application) {
          val connector = application.injector.instanceOf[TestOnlySecureMessagingConnector]

          server.stubFor(
            post(urlEqualTo(url))
              .willReturn(
                aResponse()
                  .withStatus(BAD_REQUEST)
              )
          )

          val result = connector.createCustomMessage(
            enrolmentKey, identifierValue, "firstName", "lastName", "test@mail.com", "subject", "body"
          ).futureValue

          result.status mustBe BAD_REQUEST
        }
      }
    }
  }

}
