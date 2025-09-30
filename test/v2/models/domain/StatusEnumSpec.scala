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

package v2.models.domain

import config.MockSAIndividualDetailsConfig
import play.api.libs.json.{JsString, Json}
import shared.utils.UnitSpec
import shared.utils.enums.EnumJsonSpecSupport
import v2.models.domain.StatusEnum.*

class StatusEnumSpec extends UnitSpec with EnumJsonSpecSupport with MockSAIndividualDetailsConfig {

  "JSON reads" must {
    "deserialize correctly when tax year is before 26-27" in {
      digitallyExemptTaxYearMock(2027)
      val namesAndValues = Seq(
        ("00", `No Status`),
        ("01", `MTD Mandated`),
        ("02", `MTD Voluntary`),
        ("03", Annual),
        ("04", `Non Digital`),
        ("05", Dormant),
        ("99", `MTD Exempt`))
      namesAndValues.foreach { case (name, obj) =>
        JsString(name).as[StatusEnum] shouldBe obj
      }
    }
  }

  "deserialize correctly when tax year is 26-27 or later" in {
    digitallyExemptTaxYearMock(2026)
    val namesAndValues = Seq(
      ("00", `No Status`),
      ("01", `MTD Mandated`),
      ("02", `MTD Voluntary`),
      ("03", Annual),
      ("04", `Digitally Exempt`),
      ("05", Dormant),
      ("99", `MTD Exempt`))
    namesAndValues.foreach { case (name, obj) =>
      JsString(name).as[StatusEnum] shouldBe obj
    }
  }

  "JSON formats" must {
    "support round trip when tax year is before 26-27" in {
      digitallyExemptTaxYearMock(2027)
      val namesAndValues = Seq(
        ("No Status", `No Status`),
        ("MTD Mandated", `MTD Mandated`),
        ("MTD Voluntary", `MTD Voluntary`),
        ("Annual", Annual),
        ("Non Digital", `Non Digital`),
        ("Dormant", Dormant),
        ("MTD Exempt", `MTD Exempt`)
      )
      namesAndValues.foreach { case (name, obj) =>
        val json = Json.parse(s""""$name"""")

        Json.toJson(obj) shouldBe json
        json.as[StatusEnum] shouldBe obj
      }
    }
  }

  "JSON formats" must {
    "support round trip when tax year is 26-27 or later" in {
      digitallyExemptTaxYearMock(2026)
      val namesAndValues = Seq(
        ("No Status", `No Status`),
        ("MTD Mandated", `MTD Mandated`),
        ("MTD Voluntary", `MTD Voluntary`),
        ("Annual", Annual),
        ("Digitally Exempt", `Digitally Exempt`),
        ("Dormant", Dormant),
        ("MTD Exempt", `MTD Exempt`)
      )
      namesAndValues.foreach { case (name, obj) =>
        val json = Json.parse(s""""$name"""")

        Json.toJson(obj) shouldBe json
        json.as[StatusEnum] shouldBe obj
      }
    }
  }

}
