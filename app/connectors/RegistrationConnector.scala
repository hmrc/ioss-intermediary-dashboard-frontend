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
import connectors.RegistrationConnectorHttpParser.{EtmpDisplayRegistrationResponseWithWrapper, EtmpDisplayRegistrationResponseWithWrapperReads}
import connectors.SavedPendingRegistrationHttpParser.{SavedPendingRegistrationResponse, SavedPendingRegistrationResultResponseReads}
import connectors.VatCustomerInfoHttpParser.{VatCustomerInfoResponse, VatCustomerInfoResponseReads}
import logging.Logging
import models.enrolments.EACDEnrolments
import models.etmp.RegistrationWrapper
import play.api.Configuration
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpErrorFunctions, StringContextOps}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RegistrationConnector @Inject()(config: Configuration, httpClientV2: HttpClientV2)
                                     (implicit executionContext: ExecutionContext) extends HttpErrorFunctions with Logging {


  private val baseUrl: Service = config.get[Service]("microservice.services.ioss-intermediary-dashboard")
  private val netpUrl: Service = config.get[Service]("microservice.services.ioss-netp-registration")
  private val iossIntermediaryUrl: Service = config.get[Service]("microservice.services.ioss-intermediary-registration")

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

  def getRegistration(intermediaryNumber: String)(implicit hc: HeaderCarrier): Future[RegistrationWrapper] = {
    httpClientV2.get(url"$displayRegistrationUrl/get-registration/$intermediaryNumber").execute[RegistrationWrapper]
  }

  def getNumberOfSavedUserAnswers(intermediaryNumber: String)(implicit hc: HeaderCarrier): Future[Long] = {
    httpClientV2.get(url"$netpUrl/save-for-later/count/$intermediaryNumber").execute[Long]
  }

  def displayRegistration(intermediaryNumber: String)(implicit hc: HeaderCarrier): Future[EtmpDisplayRegistrationResponseWithWrapper] = {
    httpClientV2.get(url"$displayRegistrationUrl/get-registration/$intermediaryNumber").execute[EtmpDisplayRegistrationResponseWithWrapper]
  }

  def getIntermediaryAccounts()(implicit hc: HeaderCarrier): Future[EACDEnrolments] =
    httpClientV2.get(url"$iossIntermediaryUrl/accounts").execute[EACDEnrolments]
}
