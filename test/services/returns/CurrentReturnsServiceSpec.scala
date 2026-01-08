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

package services.returns

import base.SpecBase
import connectors.ReturnStatusConnector
import models.responses.{ErrorResponse, InternalServerError}
import models.returns.CurrentReturns
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito
import org.mockito.Mockito.{times, verify, when}
import org.scalacheck.Gen
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar.mock
import uk.gov.hmrc.http.HeaderCarrier
import utils.FutureSyntax.FutureOps

import scala.concurrent.ExecutionContext.Implicits.global

class CurrentReturnsServiceSpec extends SpecBase with BeforeAndAfterEach {

  private val mockReturnStatusConnector: ReturnStatusConnector = mock[ReturnStatusConnector]

  private implicit val hc: HeaderCarrier = new HeaderCarrier()

  private val currentReturns: Seq[CurrentReturns] = Gen.listOfN(3, arbitraryCurrentReturns.arbitrary).sample.value

  override def beforeEach(): Unit = {
    Mockito.reset(mockReturnStatusConnector)
  }

  "CurrentReturnsService" - {

    ".getCurrentReturns" - {

      "must return a Seq[CurrentReturns] when the connector returns a Seq[CurrentReturns]" in {

        when(mockReturnStatusConnector.getCurrentReturns(any())(any())) thenReturn Right(currentReturns).toFuture

        val service = new CurrentReturnsService(mockReturnStatusConnector)

        val result = service.getCurrentReturns(intermediaryNumber).futureValue

        result `mustBe` currentReturns
        verify(mockReturnStatusConnector, times(1)).getCurrentReturns(eqTo(intermediaryNumber))(any())
      }

      "must return an empty Seq() when the connector returns no current returns" in {

        when(mockReturnStatusConnector.getCurrentReturns(any())(any())) thenReturn Right(Seq.empty).toFuture

        val service = new CurrentReturnsService(mockReturnStatusConnector)

        val result = service.getCurrentReturns(intermediaryNumber).futureValue

        result `mustBe` Seq.empty
        verify(mockReturnStatusConnector, times(1)).getCurrentReturns(eqTo(intermediaryNumber))(any())
      }

      "must throw an Exception when the connector returns an error" in {

        val error: ErrorResponse = InternalServerError
        val errorMessage: String = s"There was an error retrieving current returns with error: ${error.body}."

        when(mockReturnStatusConnector.getCurrentReturns(any())(any())) thenReturn Left(error).toFuture

        val service = new CurrentReturnsService(mockReturnStatusConnector)

        val result = service.getCurrentReturns(intermediaryNumber).failed

        whenReady(result) { exp =>
          exp `mustBe` a[Exception]
          exp.getMessage `mustBe` errorMessage
        }
        verify(mockReturnStatusConnector, times(1)).getCurrentReturns(eqTo(intermediaryNumber))(any())
      }
    }
  }
}
