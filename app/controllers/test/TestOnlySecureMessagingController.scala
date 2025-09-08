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

import forms.ConversationData
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.TestOnlySecureMessagingView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class TestOnlySecureMessagingController @Inject()(
                                                   override val messagesApi: MessagesApi,
                                                   val controllerComponents: MessagesControllerComponents,
                                                   view: TestOnlySecureMessagingView
                                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val form = ConversationData()

  def onPageLoad(): Action[AnyContent] = Action {
    implicit request =>

      val secureMessageAction = Call("POST", "http://localhost:9202/secure-message-stub")

      Ok(view(form, secureMessageAction))
  }
}
