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
import models.Period
import models.responses.{InvalidJson, NotFound, UnexpectedResponseStatus}
import models.saveForLater.SavedUserAnswers
import org.scalacheck.Gen
import play.api.Application
import play.api.http.Status.*
import play.api.libs.json.{JsArray, JsValue, Json}
import testutils.WireMockHelper
import uk.gov.hmrc.http.HeaderCarrier

class SaveForLaterConnectorSpec extends SpecBase with WireMockHelper {

  implicit private lazy val hc: HeaderCarrier = HeaderCarrier()

  private val savedUserAnswers: Seq[SavedUserAnswers] = Gen.listOfN(3, arbitrarySavedUserAnswers.arbitrary).sample.value

  private def application: Application = applicationBuilder()
    .configure(
      "microservice.services.ioss-returns.port" -> server.port
    )
    .build()

  "SaveForLaterConnector" - {

    ".getForIntermediary" - {

      val getUrl: String = "/ioss-returns/intermediary-save-for-later"
      val responseJson = Json.toJson(savedUserAnswers)

      "must return Right(Seq(SavedUserAnswers)) when the server responds with OK" in {

        server.stubFor(
          get(urlEqualTo(getUrl))
            .willReturn(aResponse()
              .withStatus(OK)
              .withBody(responseJson.toString)
            )
        )

        val connector = application.injector.instanceOf[SaveForLaterConnector]

        val result = connector.getForIntermediary().futureValue

        result `mustBe` Right(savedUserAnswers)
      }

      "must return Right(Seq.empty) when the server responds with OK but there are no SavedUserAnswers" in {

        server.stubFor(
          get(urlEqualTo(getUrl))
            .willReturn(aResponse()
              .withStatus(OK)
              .withBody(Json.arr().toString)
            )
        )

        val connector = application.injector.instanceOf[SaveForLaterConnector]

        val result = connector.getForIntermediary().futureValue

        result `mustBe` Right(Seq.empty)
      }

      "must return Left(InvalidJson) when the payload cannot be parsed correctly" in {

        val invalidJson: JsArray = Json.arr(Json.obj("invalid" -> "answers"))

        server.stubFor(
          get(urlEqualTo(getUrl))
            .willReturn(aResponse()
              .withStatus(OK)
              .withBody(invalidJson.toString)
            )
        )

        val connector = application.injector.instanceOf[SaveForLaterConnector]

        val result = connector.getForIntermediary().futureValue

        result `mustBe` Left(InvalidJson)
      }

      Seq(NOT_FOUND, CONFLICT, INTERNAL_SERVER_ERROR, 123).foreach { status =>
        s"must return Left(UnexpectedResponseStatus($status)) when the server returns an error" in {

          val errorMessage: String = s"Unexpected response from Intermediary saved User Answers with status $status."

          server.stubFor(
            get(urlEqualTo(getUrl))
              .willReturn(aResponse()
                .withStatus(status)
              )
          )

          val connector = application.injector.instanceOf[SaveForLaterConnector]

          val result = connector.getForIntermediary().futureValue

          result `mustBe` Left(UnexpectedResponseStatus(status, errorMessage))
        }
      }
    }

    ".delete" - {

      val iossNumber: String = savedUserAnswers.head.iossNumber
      val period: Period = savedUserAnswers.head.period
      val deleteUrl: String = s"/ioss-returns/intermediary-save-for-later/delete/$iossNumber/$period"

      val responseJson = Json.toJson(true)

      "must return Right(true) when the server responds with OK and the record has been successfully deleted" in {

        server.stubFor(
          get(urlEqualTo(deleteUrl))
            .willReturn(aResponse()
              .withStatus(OK)
              .withBody(responseJson.toString)
            )
        )

        val connector = application.injector.instanceOf[SaveForLaterConnector]

        val result = connector.delete(iossNumber, period).futureValue

        result `mustBe` Right(true)
      }

      "must return Left(InvalidJson) when the payload cannot be parsed correctly" in {

        val invalidJson: JsValue = Json.toJson("invalid")

        server.stubFor(
          get(urlEqualTo(deleteUrl))
            .willReturn(aResponse()
              .withStatus(OK)
              .withBody(invalidJson.toString)
            )
        )

        val connector = application.injector.instanceOf[SaveForLaterConnector]

        val result = connector.delete(iossNumber, period).futureValue

        result `mustBe` Left(InvalidJson)
      }

      "must return Left(NotFound) when the saved user answers for the given period don't exist" in {

        server.stubFor(
          get(urlEqualTo(deleteUrl))
            .willReturn(aResponse()
              .withStatus(NOT_FOUND)
            )
        )

        val connector = application.injector.instanceOf[SaveForLaterConnector]

        val result = connector.delete(iossNumber, period).futureValue

        result `mustBe` Left(NotFound)
      }

      Seq(CONFLICT, INTERNAL_SERVER_ERROR, 123).foreach { status =>
        s"must return Left(UnexpectedResponseStatus($status)) when the server returns an error" in {

          val errorMessage: String = s"Unexpected response when deleting Intermediary saved User Answers " +
            s"with status $status."

          server.stubFor(
            get(urlEqualTo(deleteUrl))
              .willReturn(aResponse()
                .withStatus(status)
              )
          )

          val connector = application.injector.instanceOf[SaveForLaterConnector]

          val result = connector.delete(iossNumber, period).futureValue

          result `mustBe` Left(UnexpectedResponseStatus(status, errorMessage))
        }
      }
    }
  }
}
