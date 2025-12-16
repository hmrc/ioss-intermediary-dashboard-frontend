package models

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.OptionValues
import play.api.libs.json.{JsError, JsString, Json}

class ViewOrChangePreviousRegistrationSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with OptionValues {

  "ViewOrChangePreviousRegistration" - {

    "must deserialise valid values" in {

      val gen = Gen.oneOf(ViewOrChangePreviousRegistration.values.toSeq)

      forAll(gen) {
        viewOrChangePreviousRegistration =>

          JsString(viewOrChangePreviousRegistration.toString).validate[ViewOrChangePreviousRegistration].asOpt.value mustEqual viewOrChangePreviousRegistration
      }
    }

    "must fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!ViewOrChangePreviousRegistration.values.map(_.toString).contains(_))

      forAll(gen) {
        invalidValue =>

          JsString(invalidValue).validate[ViewOrChangePreviousRegistration] mustEqual JsError("error.invalid")
      }
    }

    "must serialise" in {

      val gen = Gen.oneOf(ViewOrChangePreviousRegistration.values.toSeq)

      forAll(gen) {
        viewOrChangePreviousRegistration =>

          Json.toJson(viewOrChangePreviousRegistration) mustEqual JsString(viewOrChangePreviousRegistration.toString)
      }
    }
  }
}
