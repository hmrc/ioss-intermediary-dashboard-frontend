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

import logging.Logging
import models.responses.{ErrorResponse, InvalidJson, UnexpectedResponseStatus}
import models.securemessage.responses.SecureMessageWithCount
import play.api.http.Status.{CREATED, OK}
import play.api.libs.json.{JsError, JsSuccess}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

object SecureMessagesHttpParser extends Logging {

  type SecureMessageResultResponse = Either[ErrorResponse, SecureMessageWithCount]
  
  implicit object SecureMessageResultResponseReads extends HttpReads[SecureMessageResultResponse] {

    def operation(action: String) = action.toUpperCase match {
      case "POST" => "create"
      case "GET" => "retrieve"
      case other => other.toLowerCase
    }
    
    override def read(method: String, url: String, response: HttpResponse): SecureMessageResultResponse = {

      response.status match {
        case OK | CREATED => response.json.validate[SecureMessageWithCount] match {
          case JsSuccess(secureMessage, _) => Right(secureMessage)
          case JsError(error) =>
            logger.error(s"Failed trying to parse Secure Message JSON with error: $error")
            Left(InvalidJson)
        }

        case status =>
          logger.error(s"Received unexpected error when trying to ${operation(method)} a Secure Message with status $status and body ${response.body}")
          Left(UnexpectedResponseStatus(
            response.status,
            s"Unexpected response when trying to ${operation(method)} secure messages, status $status returned")
          )
      }
    }
  }
}
