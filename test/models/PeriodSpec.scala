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

package models

import base.SpecBase
import models.Period.getNext
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.EitherValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.mvc.{PathBindable, QueryStringBindable}

import java.time.{LocalDate, Month}

class PeriodSpec
  extends SpecBase
    with ScalaCheckPropertyChecks
    with EitherValues {

  private val pathBindable: PathBindable[Period] = implicitly[PathBindable[Period]]
  private val queryBindable: QueryStringBindable[Period] = implicitly[QueryStringBindable[Period]]

  "Period" - {

    "pathBindable" - {

      "must bind from a URL" in {

        forAll(arbitrary[Period]) {
          period =>
            pathBindable.bind("key", period.toString).value `mustBe` period
        }
      }

      "must not bind from an invalid value" in {

        pathBindable.bind("key", "invalid").left.value `mustBe` "Invalid period"
      }
    }

    "queryBindable" - {

      "must bind from a query parameter when valid period present" in {

        forAll(arbitrary[Period]) {
          period =>

            queryBindable.bind("key", Map("key" -> Seq(period.toString))) `mustBe` Some(Right(period))
        }
      }

      "must not bind from an invalid value" in {

        queryBindable.bind("key", Map("key" -> Seq("invalid"))) `mustBe` Some(Left("Invalid period"))
      }

      "must return none if no query parameter present" in {

        queryBindable.bind("key", Map("key" -> Seq.empty)) `mustBe` None
      }
    }

    "getNext" - {

      "when current period is January" in {

        val year = 2021
        val current = StandardPeriod(year, Month.JANUARY)
        val expected = StandardPeriod(year, Month.FEBRUARY)

        getNext(current) `mustBe` expected
      }

      "when current period is February" in {

        val year = 2021
        val current = StandardPeriod(year, Month.FEBRUARY)
        val expected = StandardPeriod(year, Month.MARCH)

        getNext(current) `mustBe` expected
      }

      "when current period is July" in {

        val year = 2021
        val current = StandardPeriod(year, Month.JULY)
        val expected = StandardPeriod(year, Month.AUGUST)

        getNext(current) `mustBe` expected
      }

      "when current month is December" in {

        val year = 2021
        val current = StandardPeriod(year, Month.DECEMBER)
        val expected = StandardPeriod(year + 1, Month.JANUARY)

        getNext(current) `mustBe` expected
      }
    }
  }

  ".firstDay" - {

    "must be the first of the month when the month is January" in {

      forAll(Gen.choose(2023, 2100)) {
        year =>
          val period = StandardPeriod(year, Month.JANUARY)
          period.firstDay `mustBe` LocalDate.of(year, Month.JANUARY, 1)
      }
    }
  }

  ".lastDay" - {

    "must be the last of the month when the month is January" in {

      forAll(Gen.choose(2023, 2100)) {
        year =>
          val period = StandardPeriod(year, Month.JANUARY)
          period.lastDay `mustBe` LocalDate.of(year, Month.JANUARY, 31)
      }
    }
  }
}
