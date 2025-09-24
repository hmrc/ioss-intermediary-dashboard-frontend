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
import forms.test.TestOnlySecureMessagingFormProvider
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.TestOnlySecureMessagingView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TestOnlySecureMessagingController @Inject()(
                                                   override val messagesApi: MessagesApi,
                                                   val controllerComponents: MessagesControllerComponents,
                                                   view: TestOnlySecureMessagingView,
                                                   connector: TestOnlySecureMessagingConnector,
                                                   formProvider: TestOnlySecureMessagingFormProvider
                                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val form: Form[Option[Int]] = formProvider()

  def onPageLoad(): Action[AnyContent] = Action {
    implicit request =>
      Ok(view(form))
  }

  def onSubmit(): Action[AnyContent] = Action.async { implicit request =>
    form.bindFromRequest().fold(
      formWithErrors => Future.successful(BadRequest(view(formWithErrors))),
      numberOfEmailsRequested => {
        val numberOfEmails = numberOfEmailsRequested.getOrElse(1)

        val results: Future[List[HttpResponse]] =
          Future.sequence((1 to numberOfEmails).map(_ => connector.createSecureMessage()).toList)

        results.flatMap { responses =>
          responses.find(r => r.status != 201 ) match {
            case Some(failure) =>
              Future.failed(
                new RuntimeException(s"Message creation failed with status ${failure.status}")
              )

            case None =>
              Future.successful(
                Ok(
                  s"""
                     |<h1>Messages successfully created!</h1>
                     |<p>Number of messages created: $numberOfEmails</p>
                     |<p>Number of messages created: ${responses.toString()}</p>
                     |<p><a href="${controllers.test.routes.TestOnlySecureMessagingController.onPageLoad()}">Create more messages</a></p>
                     |""".stripMargin
                ).as("text/html")
              )
          }
        }.recover { ex =>
          InternalServerError(
            s"""
               |<h1>Error creating messages</h1>
               |<p>Requested: $numberOfEmails</p>
               |<p>Error: ${ex.getMessage}</p>
               |<p><a href="${controllers.test.routes.TestOnlySecureMessagingController.onPageLoad()}">Try again</a></p>
               |""".stripMargin
          ).as("text/html")
        }
      }
    )
  }
}


