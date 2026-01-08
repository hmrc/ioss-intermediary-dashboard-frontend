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

package forms.test

import forms.mappings.Mappings
import play.api.data.Form
import play.api.data.Forms._



object TestOnlySecureMessagingFormProvider extends Mappings {

  def apply(): Form[TestOnlySecureMessagingFormProvider] =
    Form(
      mapping(
        "enrolmentKey"     -> text("Enrolment Key must not be blank"),
        "identifierValue"  -> text("Identifier Value must not be blank"),
        "numberOfMessages" -> int("Must not be blank", "Must be a whole number", "Must be a number")
      )((ek, iv, n) => TestOnlySecureMessagingFormProvider(ek, iv, n))
        (data => Some((data.enrolmentKey, data.identifierValue, data.numberOfMessages)))
    )
}

case class TestOnlySecureMessagingFormProvider(enrolmentKey: String, identifierValue: String, numberOfMessages: Int)
