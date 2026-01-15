/*
 * Copyright 2024 HM Revenue & Customs
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

package testutils

import base.SpecBase
import models.StandardPeriod
import models.returns.{PreviousRegistration, SelectedPreviousRegistration}

import java.time.YearMonth

object PreviousRegistrationData extends SpecBase {

  val previousRegistration1: PreviousRegistration = PreviousRegistration(
    "IN9001234567",
    StandardPeriod(YearMonth.of(2025, 1)),
    StandardPeriod(YearMonth.of(2025, 2))
  )

  val previousRegistration2: PreviousRegistration = PreviousRegistration(
    "IN9001234568",
    StandardPeriod(YearMonth.of(2025, 3)),
    StandardPeriod(YearMonth.of(2025, 10))
  )

  val selectedPreviousRegistration: SelectedPreviousRegistration = SelectedPreviousRegistration(userAnswersId, previousRegistration2)

  val previousRegistrations: List[PreviousRegistration] = List(previousRegistration1, previousRegistration2)
}
