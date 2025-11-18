package controllers.test

import base.SpecBase
import controllers.routes
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.ClientReturnsListView

class ClientReturnsListControllerSpec extends SpecBase {

  "ClientReturnsList Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.ClientReturnsListController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ClientReturnsListView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(request, messages(application)).toString
      }
    }
  }
}
