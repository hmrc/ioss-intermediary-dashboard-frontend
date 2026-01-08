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

import logging.Logging
import models.responses.{ErrorResponse, InvalidJson, NotFound, UnexpectedResponseStatus}
import models.saveForLater.SavedUserAnswers
import play.api.http.Status.{NOT_FOUND, OK}
import play.api.libs.json.{JsError, JsSuccess}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

object IntermediarySaveForLaterHttpParser extends Logging {

  type IntermediarySaveForLaterResponse = Either[ErrorResponse, Seq[SavedUserAnswers]]
  type DeleteIntermediarySaveForLaterResponse = Either[ErrorResponse, Boolean]

  implicit object IntermediarySaveForLaterReads extends HttpReads[IntermediarySaveForLaterResponse] {

    override def read(method: String, url: String, response: HttpResponse): IntermediarySaveForLaterResponse = {
      response.status match {
        case OK =>
          response.json.validate[Seq[SavedUserAnswers]] match {
            case JsSuccess(savedUserAnswers, _) => Right(savedUserAnswers)
            case JsError(errors) =>
              logger.warn(s"Failed trying to parse Intermediary saved user answers JSON $errors with " +
                s"response Json: ${response.json} and errors: $errors", errors)
              Left(InvalidJson)
          }

        case status =>
          logger.warn("Received unexpected error from Intermediary saved user answers")
          Left(UnexpectedResponseStatus(response.status, s"Unexpected response from Intermediary saved User Answers " +
            s"with status $status."))
      }
    }
  }

  implicit object DeleteIntermediarySaveForLaterReads extends HttpReads[DeleteIntermediarySaveForLaterResponse] {

    override def read(method: String, url: String, response: HttpResponse): DeleteIntermediarySaveForLaterResponse = {
      response.status match {
        case OK =>
          response.json.validate[Boolean] match {
            case JsSuccess(deletedAnswers, _) => Right(deletedAnswers)
            case JsError(errors) =>
              logger.warn(s"Failed trying to parse JSON $errors with " +
                s"response Json: ${response.json} and errors: $errors", errors)
              Left(InvalidJson)
          }

        case NOT_FOUND =>
          logger.warn("Received NotFound when deleting Intermediary saved user answers")
          Left(NotFound)

        case status =>
          logger.warn("Received unexpected error when deleting Intermediary saved user answers")
          Left(UnexpectedResponseStatus(response.status, s"Unexpected response when deleting Intermediary saved User Answers " +
            s"with status $status."))
      }
    }
  }
}
