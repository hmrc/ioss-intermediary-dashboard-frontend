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

import config.Service
import connectors.RegistrationConnectorHttpParser.{EtmpDisplayRegistrationResponse, EtmpDisplayRegistrationResponseReads}
import connectors.SavedPendingRegistrationHttpParser.{SavedPendingRegistrationResponse, SavedPendingRegistrationResultResponseReads}
import connectors.VatCustomerInfoHttpParser.{VatCustomerInfoResponse, VatCustomerInfoResponseReads}
import logging.Logging
import play.api.Configuration
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpErrorFunctions, StringContextOps}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RegistrationConnector @Inject()(config: Configuration, httpClientV2: HttpClientV2)
                                     (implicit executionContext: ExecutionContext) extends HttpErrorFunctions with Logging {


  private val baseUrl: Service = config.get[Service]("microservice.services.ioss-intermediary-dashboard")
  private val netpUrl: Service = config.get[Service]("microservice.services.ioss-netp-registration")

  private val displayRegistrationUrl: Service = config.get[Service]("microservice.services.ioss-intermediary-registration")

  def getVatCustomerInfo(vrn: String)(implicit hc: HeaderCarrier): Future[VatCustomerInfoResponse] = {
    httpClientV2.get(url"$baseUrl/vat-information/$vrn").execute[VatCustomerInfoResponse]
  }

  def getNumberOfPendingRegistrations(intermediaryNumber: String)(implicit hc: HeaderCarrier): Future[Long] = {
    httpClientV2.get(url"$netpUrl/pending-registrations/count/$intermediaryNumber").execute[Long]
  }

  def getPendingRegistrations(intermediaryNumber: String)(implicit hc: HeaderCarrier): Future[SavedPendingRegistrationResponse] = {
    httpClientV2.get(url"$netpUrl/pending-registrations/$intermediaryNumber")
      .execute[SavedPendingRegistrationResponse]
  }
  
  def getDisplayRegistration(intermediaryNumber: String)(implicit hc: HeaderCarrier): Future[EtmpDisplayRegistrationResponse] = {
    httpClientV2.get(url"$displayRegistrationUrl/get-registration/$intermediaryNumber")
      .execute[EtmpDisplayRegistrationResponse]
  }
}
