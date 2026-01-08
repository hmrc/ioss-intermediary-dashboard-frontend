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
import models.saveForLater.ContinueSingleClientSavedReturn
import play.api.data.FormError

class ContinueSingleClientSavedReturnFormProviderSpec extends OptionFieldBehaviours {

  private val etmpClientDetails: EtmpClientDetails = arbitraryEtmpClientDetails.arbitrary.sample.value
  private val form = new ContinueSingleClientSavedReturnFormProvider()(etmpClientDetails.clientName, etmpClientDetails.clientIossID)

  ".value" - {

    val fieldName = "value"
    val requiredKey = "continueSingleClientSavedReturn.error.required"

    behave like optionsField[ContinueSingleClientSavedReturn](
      form,
      fieldName,
      validValues  = ContinueSingleClientSavedReturn.values,
      invalidError = FormError(fieldName, "error.invalid", args = Seq(etmpClientDetails.clientName, etmpClientDetails.clientIossID))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey, args = Seq(etmpClientDetails.clientName, etmpClientDetails.clientIossID))
    )
  }
}
