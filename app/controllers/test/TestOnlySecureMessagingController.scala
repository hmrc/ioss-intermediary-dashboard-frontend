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

package controllers.test

import connectors.test.TestOnlySecureMessagingConnector
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.TestOnlySecureMessagingView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class TestOnlySecureMessagingController @Inject()(
                                                   override val messagesApi: MessagesApi,
                                                   val controllerComponents: MessagesControllerComponents,
                                                   view: TestOnlySecureMessagingView,
                                                   connector: TestOnlySecureMessagingConnector,
                                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {


  def onPageLoad(): Action[AnyContent] = Action {
    implicit request =>
      Ok(view())
  }

  def onSubmit(): Action[AnyContent] = Action.async {
    implicit request =>
      connector.sendSecureMessage().map { response =>
        response.status match {
          case 200 | 201 =>
            Ok(
              s"""
                 |<h1>Message sent successfully</h1>
                 |<p>Response status: ${response.status}</p>
                 |<p>Response body: ${response.body}</p>
                 |</div>
                 |</div>""".stripMargin).as("text/html")
          case _ =>
            Ok(
              s"""
                 |<h1>Failed to send message</h1>
                 |<p>Status: ${response.status}</p>
                 |<p>Response body: ${response.body}</p>
                 |""".stripMargin).as("text/html")
        }
      }.recover {
        case ex =>
          Ok(
            s"""
               |<h1>Error sending message</h1>
               |<p>Error: ${ex.getMessage}</p>
               """.stripMargin).as("text/html")
      }
  }
}


