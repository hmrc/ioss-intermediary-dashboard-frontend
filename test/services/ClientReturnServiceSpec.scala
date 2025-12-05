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

package services

import base.SpecBase
import connectors.ReturnStatusConnector
import models.StandardPeriod
import models.etmp.EtmpClientDetails
import models.returns.SubmissionStatus.Complete
import models.returns.{CurrentReturns, Return}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.mockito.MockitoSugar.mock
import uk.gov.hmrc.http.HeaderCarrier
import utils.FutureSyntax.FutureOps

import java.time.Month
import scala.concurrent.ExecutionContext

class ClientReturnServiceSpec extends SpecBase with MockitoSugar {
  
  implicit private lazy val hc: HeaderCarrier = HeaderCarrier()
  implicit private lazy val ec: ExecutionContext = ExecutionContext.global
  
  private val mockReturnStatusConnector: ReturnStatusConnector = mock[ReturnStatusConnector]
  private val clientReturnService: ClientReturnService = new ClientReturnService(mockReturnStatusConnector)
  
  private val client1 = EtmpClientDetails(
    clientName = "The John Rambo Salvation Army",
    clientIossID = "IM9001234567",
    clientExcluded = true
  )

  private val client2 = EtmpClientDetails(
    clientName = "Darth Vader vocal coaching ltd",
    clientIossID = "IM9001234568",
    clientExcluded = false
  )
  
  private val year = 2025
  private val completedReturns = Return(
    period = StandardPeriod(year, Month.JANUARY),
    firstDay = java.time.LocalDate.of(2025, 12, 1),
    lastDay = java.time.LocalDate.of(2025, 12, 31),
    dueDate = java.time.LocalDate.of(2025, 12, 15),
    submissionStatus = Complete,
    inProgress = false,
    isOldest = false
  )

  private val clients = Seq(client1, client2)
  
  "ClientReturnService" - {
    
    ".getReturns " - {
      
      "must return clients with completed returns" in {
        val currentReturns = Seq(
           CurrentReturns(
             iossNumber = "IM9001234567",
             incompleteReturns = Seq.empty,
             completedReturns = Seq(completedReturns)
           ),
           CurrentReturns(
             iossNumber = "IM9001234568",
             incompleteReturns = Seq.empty,
             completedReturns = Seq(completedReturns)
           )
        )

        when(mockReturnStatusConnector.getCurrentReturns(any())(any())) thenReturn Right(currentReturns).toFuture
        
        val result = clientReturnService.clientsWithCompletedReturns(clients, intermediaryNumber).futureValue

        result mustBe clients
      }
    }
  }
}
