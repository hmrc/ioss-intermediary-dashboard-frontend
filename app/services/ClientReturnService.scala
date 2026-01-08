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

package services


import connectors.ReturnStatusConnector
import models.etmp.EtmpClientDetails
import uk.gov.hmrc.http.HeaderCarrier


import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ClientReturnService @Inject()(returnStatusConnector: ReturnStatusConnector)(implicit ec: ExecutionContext) {

  def clientsWithCompletedReturns(
                                   clients: Seq[EtmpClientDetails],
                                   intermediaryNumber: String
                                 )(implicit hc: HeaderCarrier): Future[Seq[EtmpClientDetails]] = {

    returnStatusConnector.getCurrentReturns(intermediaryNumber).map {
      case Right(currentReturns) =>
       
        val clientWithCompletedReturns =
          currentReturns
            .filter(_.completedReturns.nonEmpty)
            .map(_.iossNumber)
        
        clients.filter(client => clientWithCompletedReturns.contains(client.clientIossID))

      case Left(_) => Seq.empty
    }
  }
}
