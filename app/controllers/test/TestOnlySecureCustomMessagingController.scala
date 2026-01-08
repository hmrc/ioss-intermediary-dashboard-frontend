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

package controllers.test

import connectors.test.TestOnlySecureMessagingConnector
import forms.test.TestOnlySecureCustomMessagingFormProvider
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.TestOnlySecureCustomMessagingView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TestOnlySecureCustomMessagingController @Inject()(
                                                         override val messagesApi: MessagesApi,
                                                         val controllerComponents: MessagesControllerComponents,
                                                         view: TestOnlySecureCustomMessagingView,
                                                         connector: TestOnlySecureMessagingConnector
                                                       )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val form = TestOnlySecureCustomMessagingFormProvider()

  def onPageLoad(): Action[AnyContent] = Action {
    implicit request =>
      Ok(view(form))
  }

  def onSubmit(): Action[AnyContent] = Action.async {
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors))),
        formData => {
          val enrolmentKey = formData.enrolmentKey
          val identifierValue = formData.identifierValue
          val firstName: String = formData.firstName
          val lastName: String = formData.lastName
          val emailAddress: String = formData.emailAddress
          val subject: String = formData.subject
          val body: String = formData.body
          
          val result = connector.createCustomMessage(enrolmentKey, identifierValue, firstName, lastName, emailAddress, subject, body)

          result.map { response =>
            response.status match {
              case 201 =>
                Ok(
                  s"""
                     |<h1>Message successfully created!</h1>
                     |<p><strong>Recipient:</strong> $firstName $lastName</p>
                     |<p><strong>Sender Email:</strong>$emailAddress</p>
                     |<p><strong>Subject:</strong> $subject</p>
                     |<p><strong>Response Status:</strong> ${response.status}</p>
                     |<p><a href="${controllers.test.routes.TestOnlySecureCustomMessagingController.onPageLoad()}">Create another message</a></p>
                     |""".stripMargin
                ).as("text/html")
            }
          }.recover { ex =>
            InternalServerError(
              s"""
                 |<h1>Error creating message</h1>
                 |<p><strong>Error:</strong> ${ex.getMessage}</p>
                 |<p><a href="${controllers.test.routes.TestOnlySecureCustomMessagingController.onPageLoad()}">Go back</a></p>
                 |""".stripMargin
            ).as("text/html")
          }
        }
      )
  }
}
