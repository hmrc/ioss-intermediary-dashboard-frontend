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

import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.writeableOf_JsValue
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

class TestOnlySecureCustomMessagingConnector @Inject()(
                                                  httpClientV2: HttpClientV2,
                                                )(implicit ec: ExecutionContext) {

  private val secureMessageUrl = "http://localhost:9051/secure-messaging/v4/message"

  def createSecureCustomMessage(
                           firstName: String,
                           lastName: String,
                           emailAddress: String,
                           subject: String,
                           body: String
                         )(implicit hc: HeaderCarrier): Future[HttpResponse] = {

    val baseJsonPayload: JsObject = Json.obj(
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
          "line1" -> s"$firstName",
          "line2" -> s"$lastName",
        ),
        "email" -> s"$emailAddress",
        "regime" -> "ioss"
      ),
      "messageType" -> "mailout-batch",
      "details" -> Json.obj(
        "formId" -> "M08aGIOSS",
        "sourceData" -> "test-source-data",
        "batchId" -> "IOSSMessage",
      ),
      "content" -> Json.arr(
        Json.obj(
          "lang" -> "en",
          "subject" -> s"$subject",
          "body" -> s"$body"
        )
      ),
      "language" -> "en"
    )
    httpClientV2
      .post(url"$secureMessageUrl")
      .withBody(baseJsonPayload)
      .execute[HttpResponse]
  }

  private def random18Digit(): BigInt = {
    val part1 = Random.between(100000, 999999)
    val part2 = Random.between(100000, 999999)
    val part3 = Random.between(100000, 999999)
    BigInt(s"$part1$part2$part3")
  }
}
