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

package controllers.actions

import connectors.RegistrationConnector
import models.requests.{OptionalDataRegistrationRequest, OptionalDataRequest}
import play.api.mvc.{ActionRefiner, Result, Results}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}


class GetOptionalDataRegistrationAction @Inject()(
                                                   registrationConnector: RegistrationConnector
                                                 )(implicit val executionContext: ExecutionContext)
  extends ActionRefiner[OptionalDataRequest, OptionalDataRegistrationRequest] {

  override protected def refine[A](
                                    request: OptionalDataRequest[A]
                                  ): Future[Either[Result, OptionalDataRegistrationRequest[A]]] = {

    implicit val hc: HeaderCarrier =
      HeaderCarrierConverter.fromRequestAndSession(
        request.request,
        request.request.session
      )

    registrationConnector
      .displayRegistration(request.intermediaryNumber)
      .map {
        case Right(registrationWrapper) =>
          Right(
            OptionalDataRegistrationRequest(
              request = request.request,
              userId = request.userId,
              userAnswers = request.userAnswers,
              enrolments = request.enrolments,
              vrn = request.vrn,
              intermediaryNumber = request.intermediaryNumber,
              registrationWrapper = registrationWrapper
            )
          )

        case Left(error) =>
          Left(Results.InternalServerError)
      }
  }
}

