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

package models

import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.govukfrontend.views.viewmodels.select.SelectItem
import viewmodels.govuk.select.*

case class Country(code: String, name: String)

case class CountryWithValidationDetails(country: Country, vrnRegex: String, messageInput: String, exampleVrn: String)

object Country {

  implicit val format: OFormat[Country] = Json.format[Country]
  
  val euCountries: Seq[Country] = Seq(
    Country("AT", "Austria"),
    Country("BE", "Belgium"),
    Country("BG", "Bulgaria"),
    Country("HR", "Croatia"),
    Country("CY", "Cyprus"),
    Country("CZ", "Czech Republic"),
    Country("DK", "Denmark"),
    Country("EE", "Estonia"),
    Country("FI", "Finland"),
    Country("FR", "France"),
    Country("DE", "Germany"),
    Country("EL", "Greece"),
    Country("HU", "Hungary"),
    Country("IE", "Ireland"),
    Country("IT", "Italy"),
    Country("LV", "Latvia"),
    Country("LT", "Lithuania"),
    Country("LU", "Luxembourg"),
    Country("MT", "Malta"),
    Country("NL", "Netherlands"),
    Country("PL", "Poland"),
    Country("PT", "Portugal"),
    Country("RO", "Romania"),
    Country("SK", "Slovakia"),
    Country("SI", "Slovenia"),
    Country("ES", "Spain"),
    Country("SE", "Sweden")
  )

  val euCountrySelectItems: Seq[SelectItem] =
    SelectItem(value = Some("")) +:
      euCountries.map {
        country =>
          SelectItemViewModel(
            value = country.code,
            text = country.name
          )
      }
    
  val allCountries: Seq[Country] = Seq(
    Country("AF", "Afghanistan"),
    Country("AL", "Albania"),
    Country("DZ", "Algeria"),
    Country("AD", "Andorra"),
    Country("AO", "Angola"),
    Country("AG", "Antigua and Barbuda"),
    Country("AR", "Argentina"),
    Country("AM", "Armenia"),
    Country("AU", "Australia"),
    Country("AT", "Austria"),
    Country("AZ", "Azerbaijan"),
    Country("BH", "Bahrain"),
    Country("BD", "Bangladesh"),
    Country("BB", "Barbados"),
    Country("BY", "Belarus"),
    Country("BE", "Belgium"),
    Country("BZ", "Belize"),
    Country("BJ", "Benin"),
    Country("BT", "Bhutan"),
    Country("BO", "Bolivia"),
    Country("BA", "Bosnia and Herzegovina"),
    Country("BW", "Botswana"),
    Country("BR", "Brazil"),
    Country("BN", "Brunei"),
    Country("BG", "Bulgaria"),
    Country("BF", "Burkina Faso"),
    Country("BI", "Burundi"),
    Country("KH", "Cambodia"),
    Country("CM", "Cameroon"),
    Country("CA", "Canada"),
    Country("CV", "Cape Verde"),
    Country("CF", "Central African Republic"),
    Country("TD", "Chad"),
    Country("CL", "Chile"),
    Country("CN", "China"),
    Country("CO", "Colombia"),
    Country("KM", "Comoros"),
    Country("CG", "Congo"),
    Country("CD", "Congo (Democratic Republic)"),
    Country("CR", "Costa Rica"),
    Country("HR", "Croatia"),
    Country("CU", "Cuba"),
    Country("CY", "Cyprus"),
    Country("CZ", "Czechia"),
    Country("DK", "Denmark"),
    Country("DJ", "Djibouti"),
    Country("DM", "Dominica"),
    Country("DO", "Dominican Republic"),
    Country("TL", "East Timor"),
    Country("EC", "Ecuador"),
    Country("EG", "Egypt"),
    Country("SV", "El Salvador"),
    Country("GQ", "Equatorial Guinea"),
    Country("ER", "Eritrea"),
    Country("EE", "Estonia"),
    Country("SZ", "Eswatini"),
    Country("ET", "Ethiopia"),
    Country("FJ", "Fiji"),
    Country("FI", "Finland"),
    Country("FR", "France"),
    Country("GA", "Gabon"),
    Country("GE", "Georgia"),
    Country("DE", "Germany"),
    Country("GH", "Ghana"),
    Country("EL", "Greece"),
    Country("GD", "Grenada"),
    Country("GT", "Guatemala"),
    Country("GN", "Guinea"),
    Country("GW", "Guinea-Bissau"),
    Country("GY", "Guyana"),
    Country("HT", "Haiti"),
    Country("HN", "Honduras"),
    Country("HU", "Hungary"),
    Country("IS", "Iceland"),
    Country("IN", "India"),
    Country("ID", "Indonesia"),
    Country("IR", "Iran"),
    Country("IQ", "Iraq"),
    Country("IE", "Ireland"),
    Country("IL", "Israel"),
    Country("IT", "Italy"),
    Country("CI", "Ivory Coast"),
    Country("JM", "Jamaica"),
    Country("JP", "Japan"),
    Country("JO", "Jordan"),
    Country("KZ", "Kazakhstan"),
    Country("KE", "Kenya"),
    Country("KI", "Kiribati"),
    Country("XK", "Kosovo"),
    Country("KW", "Kuwait"),
    Country("KG", "Kyrgyzstan"),
    Country("LA", "Laos"),
    Country("LV", "Latvia"),
    Country("LB", "Lebanon"),
    Country("LS", "Lesotho"),
    Country("LR", "Liberia"),
    Country("LY", "Libya"),
    Country("LI", "Liechtenstein"),
    Country("LT", "Lithuania"),
    Country("LU", "Luxembourg"),
    Country("MG", "Madagascar"),
    Country("MW", "Malawi"),
    Country("MY", "Malaysia"),
    Country("MV", "Maldives"),
    Country("ML", "Mali"),
    Country("MT", "Malta"),
    Country("MH", "Marshall Islands"),
    Country("MR", "Mauritania"),
    Country("MU", "Mauritius"),
    Country("MX", "Mexico"),
    Country("FM", "Federated States of Micronesia"),
    Country("MD", "Moldova"),
    Country("MC", "Monaco"),
    Country("MN", "Mongolia"),
    Country("ME", "Montenegro"),
    Country("MA", "Morocco"),
    Country("MZ", "Mozambique"),
    Country("MM", "Myanmar (Burma)"),
    Country("NA", "Namibia"),
    Country("NR", "Nauru"),
    Country("NP", "Nepal"),
    Country("NL", "Netherlands"),
    Country("NZ", "New Zealand"),
    Country("NI", "Nicaragua"),
    Country("NE", "Niger"),
    Country("NG", "Nigeria"),
    Country("KP", "North Korea"),
    Country("MK", "North Macedonia"),
    Country("NO", "Norway"),
    Country("OM", "Oman"),
    Country("PK", "Pakistan"),
    Country("PW", "Palau"),
    Country("PA", "Panama"),
    Country("PG", "Papua New Guinea"),
    Country("PY", "Paraguay"),
    Country("PE", "Peru"),
    Country("PH", "Philippines"),
    Country("PL", "Poland"),
    Country("PT", "Portugal"),
    Country("QA", "Qatar"),
    Country("RO", "Romania"),
    Country("RU", "Russia"),
    Country("RW", "Rwanda"),
    Country("WS", "Samoa"),
    Country("SM", "San Marino"),
    Country("ST", "Sao Tome and Principe"),
    Country("SA", "Saudi Arabia"),
    Country("SN", "Senegal"),
    Country("RS", "Serbia"),
    Country("SC", "Seychelles"),
    Country("SL", "Sierra Leone"),
    Country("SG", "Singapore"),
    Country("SK", "Slovakia"),
    Country("SI", "Slovenia"),
    Country("SB", "Solomon Islands"),
    Country("SO", "Somalia"),
    Country("ZA", "South Africa"),
    Country("KR", "South Korea"),
    Country("SS", "South Sudan"),
    Country("ES", "Spain"),
    Country("LK", "Sri Lanka"),
    Country("KN", "St Kitts and Nevis"),
    Country("LC", "St Lucia"),
    Country("VC", "St Vincent"),
    Country("SD", "Sudan"),
    Country("SR", "Suriname"),
    Country("SE", "Sweden"),
    Country("CH", "Switzerland"),
    Country("SY", "Syria"),
    Country("TJ", "Tajikistan"),
    Country("TZ", "Tanzania"),
    Country("TH", "Thailand"),
    Country("BS", "The Bahamas"),
    Country("GM", "The Gambia"),
    Country("TG", "Togo"),
    Country("TO", "Tonga"),
    Country("TT", "Trinidad and Tobago"),
    Country("TN", "Tunisia"),
    Country("TR", "Turkey"),
    Country("TM", "Turkmenistan"),
    Country("TV", "Tuvalu"),
    Country("UG", "Uganda"),
    Country("UA", "Ukraine"),
    Country("AE", "United Arab Emirates"),
    Country("GB", "United Kingdom"),
    Country("US", "United States"),
    Country("UY", "Uruguay"),
    Country("UZ", "Uzbekistan"),
    Country("VU", "Vanuatu"),
    Country("VA", "Vatican City"),
    Country("VE", "Venezuela"),
    Country("VN", "Vietnam"),
    Country("YE", "Yemen"),
    Country("ZM", "Zambia"),
    Country("ZW", "Zimbabwe")
  )

  val allCountriesSelectItems: Seq[SelectItem] = {
    SelectItem(value = Some("")) +:
      allCountries.map {
        country =>
          SelectItemViewModel(
            value = country.code,
            text = country.name
          )
      }
  }

  val internationalCountries: Seq[Country] =
    allCountries.filterNot(_.code == "GB")

  def getCountryName(countryCode: String): String = euCountries.filter(_.code == countryCode).map(_.name).head
}