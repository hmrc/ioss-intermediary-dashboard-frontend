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

package forms.test

import forms.mappings.Mappings
import play.api.data.Form
import play.api.data.Forms.*

object TestOnlySecureCustomMessagingFormProvider extends Mappings {

  val emailPattern = """^(?:[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21\x23-\x5b\x5d-\x7f]|\\[\x01-\x09\x0b\x0c\x0e-\x7f])*\")@(?:(?:[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?\.)+[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?|\[(?:(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9]))\.){3}(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9])|[a-zA-Z0-9-]*[a-zA-Z0-9]:(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21-\x5a\x53-\x7f]|\\[\x01-\x09\x0b\x0c\x0e-\x7f])+)\])$"""

  def apply(): Form[TestOnlySecureCustomMessagingFormProvider] =
    Form[TestOnlySecureCustomMessagingFormProvider](
      mapping(
        "firstName" -> text("First name required"),
        "lastName" -> text("Last name required"),
        "emailAddress" -> text("Email required").verifying(firstError(
          maxLength(50, "Email address must be 50 characters or less"),
          regexp(emailPattern, "Please enter a valid email address")),
        ),
    "subject" -> text("Subject required"),
    "body" -> text("Email body required"),
      )(TestOnlySecureCustomMessagingFormProvider.apply)(
        c => Some(Tuple.fromProductTyped[TestOnlySecureCustomMessagingFormProvider](c))
      )

    )
}

case class TestOnlySecureCustomMessagingFormProvider(
                                                      firstName: String,
                                                      lastName: String,
                                                      emailAddress: String,
                                                      subject: String,
                                                      body: String
                                                    )
