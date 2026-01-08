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

import models.etmp.EtmpClientDetails
import models.returns.{CurrentReturns, SubmissionStatus}

object GetOutstandingClientDetails {

  def getOutstandingClientDetailsForStatus(
                                            currentReturns: Seq[CurrentReturns],
                                            status: SubmissionStatus,
                                            clientDetails: Seq[EtmpClientDetails]
                                          ): Seq[EtmpClientDetails] = {
    val iossNumbersWithDueReturns: Seq[String] = currentReturns.filter { currentReturn =>
      currentReturn.incompleteReturns.exists(_.submissionStatus == status)
    }.map(_.iossNumber)

    clientDetails.filter { clientDetails =>
      iossNumbersWithDueReturns.contains(clientDetails.clientIossID)
    }
  }
}
