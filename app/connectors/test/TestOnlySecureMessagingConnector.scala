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
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TestOnlySecureMessagingConnector @Inject()(
                                                  httpClientV2: HttpClientV2,
                                                )(implicit ec: ExecutionContext) {

  private val secureMessageUrl = "http://localhost:9051/secure-messaging/v4/message"


  def sendSecureMessage()(implicit hc: HeaderCarrier): Future[HttpResponse] = {

    val jsonPayload: JsValue = Json.parse(
      """{
        |  "externalRef": {
        |    "id": "AJD132324532312318900",
        |    "source": "gmc"
        |  },
        |  "recipient": {
        |    "taxIdentifier": {
        |      "name": "HMRC-IOSS-NETP",
        |      "value": "IM9001234567"
        |    },
        |    "name": {
        |      "line1": "Bob",
        |      "line2": "Jones"
        |    },
        |    "email": "chandan.ray+M08aGIOSS@digital.hmrc.gov.uk",
        |    "regime": "ioss"
        |  },
        |  "messageType": "mailout-batch",
        |  "details": {
        |    "formId": "M08aGIOSS",
        |    "sourceData": "WW91IG5lZWQgdG8gZmlsZSBhIFNlbGYgQXNzZXNzbWVudCB0YXggcmV0dXJuIGZvciB0aGUgMjAyNCB0byAyMDI1IHRheCB5ZWFyIGlmIHlvdSBoYXZlbid0IGFscmVhZHkuIFRoZSB0YXggeWVhciBlbmRlZCBvbiA1IEFwcmlsIDIwMjUuCgpZb3UgbXVzdCBmaWxlIHlvdXIgb25saW5lIHJldHVybiBieSAzMSBKYW51YXJ5IDIwMjYuCgpJZiB5b3UndmUgYWxyZWFkeSBjb21wbGV0ZWQgeW91ciB0YXggcmV0dXJuIGZvciB0aGUgMjAyNCB0byAyMDI1IHRheCB5ZWFyLCBvciB3ZSd2ZSB0b2xkIHlvdSB0aGF0IHlvdSBkb24ndCBuZWVkIHRvIHNlbmQgdXMgYSAyMDI0IHRvIDIwMjUgdGF4IHJldHVybiwgeW91IGRvbid0IG5lZWQgdG8gZG8gYW55dGhpbmcgZWxzZS4KCllvdSBjYW4gcGF5IHRocm91Z2ggeW91ciBQYXkgQXMgWW91IEVhcm4gdGF4IGNvZGUgaWYgeW91IG93ZSBsZXNzIHRoYW4",
        |    "batchId": "IOSSMessage",
        |    "issueDate": "2025-08-01"
        |  },
        |  "content": [
        |    {
        |      "lang": "en",
        |      "subject": "Import One Stop Shop (IOSS)",
        |      "body": "test email 3"
        |    }
        |  ],
        |  "language": "en"
        |}""".stripMargin
    )

    httpClientV2
      .post(url"$secureMessageUrl")
      .withBody(jsonPayload)
      .execute[HttpResponse]
  }
}