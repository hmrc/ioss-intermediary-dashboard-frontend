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

package generators

import models.{Country, DesAddress}
import models.domain.ModelHelpers.normaliseSpaces
import models.domain.VatCustomerInfo
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import org.scalacheck.Gen.{choose, listOfN}
import org.scalatest.EitherValues

import java.time.{Instant, LocalDate, ZoneOffset}

trait ModelGenerators extends EitherValues {

  private val maxFieldLength: Int = 35

  private def commonFieldString(maxLength: Int): Gen[String] = (for {
    length <- choose(1, maxLength)
    chars <- listOfN(length, commonFieldSafeInputs)
  } yield chars.mkString).retryUntil(_.trim.nonEmpty)

  private def commonFieldSafeInputs: Gen[Char] = Gen.oneOf(
    Gen.alphaNumChar,
    Gen.oneOf('À' to 'ÿ'),
    Gen.const('.'),
    Gen.const(','),
    Gen.const('/'),
    Gen.const('’'),
    Gen.const('\''),
    Gen.const('"'),
    Gen.const('_'),
    Gen.const('&'),
    Gen.const(' '),
    Gen.const('\'')
  )


  def datesBetween(min: LocalDate, max: LocalDate): Gen[LocalDate] = {

    def toMillis(date: LocalDate): Long =
      date.atStartOfDay.atZone(ZoneOffset.UTC).toInstant.toEpochMilli

    Gen.choose(toMillis(min), toMillis(max)).map {
      millis =>
        Instant.ofEpochMilli(millis).atOffset(ZoneOffset.UTC).toLocalDate
    }
  }
  
  implicit lazy val arbitraryDate: Arbitrary[LocalDate] = {
    Arbitrary {
      datesBetween(LocalDate.of(2021, 7, 1), LocalDate.of(2023, 12, 31))
    }
  }

  implicit lazy val arbitraryDesAddress: Arbitrary[DesAddress] =
    Arbitrary {
      for {
        line1 <- commonFieldString(maxFieldLength)
        line2 <- Gen.option(commonFieldString(maxFieldLength))
        line3 <- Gen.option(commonFieldString(maxFieldLength))
        line4 <- Gen.option(commonFieldString(maxFieldLength))
        line5 <- Gen.option(commonFieldString(maxFieldLength))
        postCode <- Gen.option(arbitrary[String])
        country <- Gen.oneOf(Country.internationalCountries.map(_.code))
      } yield DesAddress(
        normaliseSpaces(line1),
        normaliseSpaces(line2),
        normaliseSpaces(line3),
        normaliseSpaces(line4),
        normaliseSpaces(line5),
        normaliseSpaces(postCode),
        country
      )
    }

  implicit val arbitraryVatCustomerInfo: Arbitrary[VatCustomerInfo] = {
    Arbitrary {
      for {
        desAddress <- arbitraryDesAddress.arbitrary
        registrationDate <- arbitraryDate.arbitrary
        organisationName <- Gen.alphaStr
        individualName <- Gen.alphaStr
        singleMarketIndicator <- arbitrary[Boolean]
      } yield {
        VatCustomerInfo(
          desAddress = desAddress,
          registrationDate = registrationDate,
          organisationName = Some(organisationName),
          individualName = Some(individualName),
          singleMarketIndicator = singleMarketIndicator,
          deregistrationDecisionDate = None
        )
      }
    }
  }
}
