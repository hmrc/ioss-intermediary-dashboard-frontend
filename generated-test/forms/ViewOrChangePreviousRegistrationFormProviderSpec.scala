package forms

import forms.behaviours.OptionFieldBehaviours
import models.ViewOrChangePreviousRegistration
import play.api.data.FormError

class ViewOrChangePreviousRegistrationFormProviderSpec extends OptionFieldBehaviours {

  val form = new ViewOrChangePreviousRegistrationFormProvider()()

  ".value" - {

    val fieldName = "value"
    val requiredKey = "viewOrChangePreviousRegistration.error.required"

    behave like optionsField[ViewOrChangePreviousRegistration](
      form,
      fieldName,
      validValues  = ViewOrChangePreviousRegistration.values,
      invalidError = FormError(fieldName, "error.invalid")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
