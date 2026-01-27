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

package controllers.actions

import models.requests.RegistrationRequest
import play.api.mvc.{ActionFilter, Result}
import play.api.mvc.Results.Redirect
import play.api.Configuration
import utils.FutureSyntax.FutureOps

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class FeatureEnabledFilter(featureName: String, configuration: Configuration)(implicit val executionContext: ExecutionContext)
  extends ActionFilter[RegistrationRequest] {

  override protected def filter[A](request: RegistrationRequest[A]): Future[Option[Result]] = {
    val featureEnabled = configuration.get[Boolean](s"features.$featureName.enabled")
    if (!featureEnabled) {
      Some(Redirect(controllers.routes.CannotUseNotAnIntermediaryController.onPageLoad().url)).toFuture
    } else {
      None.toFuture
    }
  }
}

class FeatureEnabledFilterProvider @Inject()(
                                              configuration: Configuration
                                            )(implicit ec: ExecutionContext) {

  def apply(featureName: String): FeatureEnabledFilter = new FeatureEnabledFilter(featureName, configuration)
}
