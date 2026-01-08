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

package connectors

import config.Service
import connectors.IntermediarySaveForLaterHttpParser.{DeleteIntermediarySaveForLaterReads, DeleteIntermediarySaveForLaterResponse, IntermediarySaveForLaterReads, IntermediarySaveForLaterResponse}
import logging.Logging
import models.Period
import play.api.Configuration
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpErrorFunctions, StringContextOps}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SaveForLaterConnector @Inject()(
                                       httpClientV2: HttpClientV2,
                                       config: Configuration
                                     )(implicit executionContext: ExecutionContext) extends HttpErrorFunctions with Logging {

  private val iossReturnsUrl: Service = config.get[Service]("microservice.services.ioss-returns")

  def getForIntermediary()(implicit hc: HeaderCarrier): Future[IntermediarySaveForLaterResponse] = {
    httpClientV2.get(url"$iossReturnsUrl/intermediary-save-for-later").execute[IntermediarySaveForLaterResponse]
  }

  def delete(iossNumber: String, period: Period)(implicit hc: HeaderCarrier): Future[DeleteIntermediarySaveForLaterResponse] = {
    httpClientV2.get(url"$iossReturnsUrl/intermediary-save-for-later/delete/$iossNumber/$period")
      .execute[DeleteIntermediarySaveForLaterResponse]
  }
}
