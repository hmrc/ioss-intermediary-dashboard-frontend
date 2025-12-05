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

package services.returns

import connectors.ReturnStatusConnector
import models.returns.CurrentReturns
import play.api.Logging
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CurrentReturnsService @Inject()(
                                       returnStatusConnector: ReturnStatusConnector
                                     )(implicit ec: ExecutionContext) extends Logging {

  def getCurrentReturns(
                         intermediaryNumber: String
                       )(implicit hc: HeaderCarrier): Future[Seq[CurrentReturns]] = {
    for {
      returnStatusResponse <- returnStatusConnector.getCurrentReturns(intermediaryNumber)
    } yield {
      returnStatusResponse match {
        case Right(allCurrentReturns) => allCurrentReturns

        case Left(error) =>
          val errorMessage: String = s"There was an error retrieving current returns with error: ${error.body}."
          val exception = new Exception(errorMessage)
          logger.error(errorMessage)
          throw exception
      }
    }
  }
}
