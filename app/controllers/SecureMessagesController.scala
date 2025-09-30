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

package controllers

import com.google.inject.Inject
import connectors.test.TestOnlySecureMessagingConnector
import controllers.actions.AuthenticatedControllerComponents
import logging.Logging
import models.securemessage.{CustomerEnrolment, MessageFilter}
import pages.Waypoints
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.SecureMessagesView
import utils.FutureSyntax.FutureOps

import scala.concurrent.ExecutionContext

class SecureMessagesController @Inject()(
                                        override val messagesApi: MessagesApi,
                                        val controllerComponents: MessagesControllerComponents,
                                        cc: AuthenticatedControllerComponents,
                                        testOnlySecureMessagingConnector: TestOnlySecureMessagingConnector,
                                        view: SecureMessagesView
                                        )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging {

  def onPageLoad(waypoints: Waypoints): Action[AnyContent] = cc.actionBuilder.async {
    implicit request =>

      val taxIdentifiers = "HMRC-IOSS-INT"

      testOnlySecureMessagingConnector.getMessages(taxIdentifiers = Some(taxIdentifiers)).flatMap {
        case Right(secureMessages) =>
          Ok(view(secureMessages)).toFuture

        case Left(errors) =>
          val message: String = s"Received an unexpected error when trying to retrieve secure messages: $errors."
          val exception: Exception = new Exception(message)
          logger.error(exception.getMessage, exception)
          throw exception
      }
  }
}
