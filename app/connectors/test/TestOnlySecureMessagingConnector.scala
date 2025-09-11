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

package connectors.test

import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.writeableOf_JsValue
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

class TestOnlySecureMessagingConnector @Inject()(
                                                  httpClientV2: HttpClientV2,
                                                )(implicit ec: ExecutionContext) {

  private val secureMessageUrl = "http://localhost:9051/secure-messaging/v4/message"

  private def random18Digit(): BigInt = {
    val part1 = Random.between(100000, 999999)
    val part2 = Random.between(100000, 999999)
    val part3 = Random.between(100000, 999999)
    BigInt(s"$part1$part2$part3")
  }


  def sendSecureMessage()(implicit hc: HeaderCarrier): Future[HttpResponse] = {

    val jsonPayload: JsValue = Json.obj(
      "externalRef" -> Json.obj(
        "id" -> s"AJD${random18Digit()}",
        "source" -> "gmc"
      ),
      "recipient" -> Json.obj(
        "taxIdentifier" -> Json.obj(
          "name" -> "HMRC-IOSS-INT",
          "value" -> "IN9001234567"
        ),
        "name" -> Json.obj(
          "line1" -> "Bob",
          "line2" -> "Jones"
        ),
        "email" -> "chandan.ray+M08aGIOSS@digital.hmrc.gov.uk",
        "regime" -> "ioss"
      ),
      "messageType" -> "mailout-batch",
      "details" -> Json.obj(
        "formId" -> "M08aGIOSS",
        "sourceData" -> "test-source-data",
        "batchId" -> "IOSSMessage",
        "issueDate" -> "2025-08-01"
      ),
      "content" -> Json.arr(
        Json.obj(
          "lang" -> "en",
          "subject" -> "Import One Stop Shop (IOSS)",
          "body" -> s"${random18Digit()}" // placeholder: body needs to be unique, ensures successful creation
        )
      ),
      "language" -> "en"
    )

    httpClientV2
      .post(url"$secureMessageUrl")
      .withBody(jsonPayload)
      .execute[HttpResponse]
  }
}