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

import play.api.libs.json.{JsArray, JsObject, Json}
import play.api.libs.ws.writeableOf_JsValue
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

class TestOnlySecureMessagingConnector @Inject()(
                                                  httpClientV2: HttpClientV2,
                                                )(implicit ec: ExecutionContext) {

  private val secureMessageUrl = "http://localhost:9051/secure-messaging/v4/message"

  private def baseJsonPayload: JsObject = Json.obj(
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
      "email" -> "test@mail.com",
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
        "subject" -> "Import One Stop Shop (IOSS)",
        "body" -> s"test email - unique ID: ${random18Digit()}"
      )
    ),
    "language" -> "en"
  )

  def createBulkMessages()
                        (implicit hc: HeaderCarrier): Future[HttpResponse] = {
    httpClientV2
      .post(url"$secureMessageUrl")
      .withBody(baseJsonPayload)
      .execute[HttpResponse]
  }

  def createCustomMessage(
      firstName: String,
      lastName: String,
      emailAddress: String,
      subject: String,
      body: String
  )(implicit hc: HeaderCarrier): Future[HttpResponse] = {

    val updatedName = (baseJsonPayload \ "recipient" \ "name").as[JsObject] ++ Json.obj(
      "line1" -> firstName,
      "line2" -> lastName
    )

    val updatedRecipient = (baseJsonPayload \ "recipient").as[JsObject] ++ Json.obj(
      "name" -> updatedName,
      "email" -> emailAddress
    )

    val originalContent = (baseJsonPayload \ "content").as[JsArray]
    val updatedFirstContent = originalContent.head.as[JsObject] ++ Json.obj(
      "subject" -> subject,
      "body" -> body
    )

    val updatedContent = Json.arr(updatedFirstContent)

    val jsonPayload: JsObject = baseJsonPayload ++ Json.obj(
      "recipient" -> updatedRecipient,
      "content" -> updatedContent
    )
    httpClientV2
      .post(url"$secureMessageUrl")
      .withBody(jsonPayload)
      .execute[HttpResponse]
  }


  private def random18Digit(): BigInt = {
    val part1 = Random.between(100000, 999999)
    val part2 = Random.between(100000, 999999)
    val part3 = Random.between(100000, 999999)
    BigInt(s"$part1$part2$part3")
  }
}