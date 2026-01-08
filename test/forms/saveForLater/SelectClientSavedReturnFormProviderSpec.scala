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

package forms.saveForLater

import forms.behaviours.OptionFieldBehaviours
import models.etmp.EtmpClientDetails
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import play.api.data.{Form, FormError}

class SelectClientSavedReturnFormProviderSpec extends OptionFieldBehaviours {

  private val etmpClientDetails: Seq[EtmpClientDetails] = Gen.listOfN(3, arbitraryEtmpClientDetails.arbitrary).sample.value
  private val form: Form[EtmpClientDetails] = new SelectClientSavedReturnFormProvider()(etmpClientDetails)

  ".value" - {

    val fieldName = "value"
    val requiredKey = "selectClientSavedReturn.error.required"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      Gen.oneOf(etmpClientDetails).map(_.clientIossID)
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    "must not bind any values other than valid ioss number" in {

      val invalidAnswers = arbitrary[String] suchThat (x => !etmpClientDetails.exists(_.clientIossID == x))

      forAll(invalidAnswers) {
        answer =>
          val result = form.bind(Map("value" -> answer)).apply(fieldName)
          result.errors must contain only FormError(fieldName, requiredKey)
      }
    }
  }
}
