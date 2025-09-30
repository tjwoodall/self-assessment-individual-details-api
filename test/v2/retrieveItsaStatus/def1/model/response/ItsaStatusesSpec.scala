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

package v2.retrieveItsaStatus.def1.model.response

import play.api.libs.json.{JsValue, Json}
import shared.utils.UnitSpec
import v2.models.domain.StatusEnum.*
import v2.models.domain.StatusReasonEnum.*

class ItsaStatusesSpec extends UnitSpec {

  private val itsaStatusDetails: ItsaStatusDetails = ItsaStatusDetails("2018-01-01", `MTD Voluntary`, `MTD ITSA Opt-In`, Some(50000.25))
  private val itsaStatuses: ItsaStatuses           = ItsaStatuses("2017", Some(Seq(itsaStatusDetails)))
  private val minimalItsaStatuses: ItsaStatuses    = ItsaStatuses("2017", None)

  private val itsaStatusDetailsWriteJson: JsValue = Json.parse(
    s"""
      |{
      | "submittedOn": "2018-01-01",
      | "status": "MTD Voluntary",
      | "statusReason": "MTD ITSA Opt-In",
      | "businessIncome2YearsPrior": 50000.25
      |}
      |""".stripMargin
  )

  private val itsaStatusDetailsReadJson: JsValue = Json.parse(
    s"""
       |{
       | "submittedOn": "2018-01-01",
       | "status": "MTD Voluntary",
       | "statusReason": "MTD ITSA Opt-In",
       | "businessIncomePriorTo2Years": 50000.25
       |}
       |""".stripMargin
  )

  private val itsaStatusesWriteJson: JsValue = Json.parse(
    s"""
       |{
       | "taxYear": "2017",
       | "itsaStatusDetails": [$itsaStatusDetailsWriteJson]
       |}
       |""".stripMargin
  )

  private val itsaStatusesReadJson: JsValue = Json.parse(
    s"""
       |{
       | "taxYear": "2017",
       | "itsaStatusDetails": [$itsaStatusDetailsReadJson]
       |}
       |""".stripMargin
  )

  private val minimalItsaStatusesJson: JsValue = Json.parse(
    s"""
       |{
       | "taxYear": "2017"
       |}
       |""".stripMargin
  )

  "ItsaStatus" when {

    "the full model is present" should {
      "correctly write the model to json" in {
        Json.toJson(itsaStatuses) shouldBe itsaStatusesWriteJson
      }
    }

    "the minimal model is present" should {
      "correctly write the model to json" in {
        Json.toJson(minimalItsaStatuses) shouldBe minimalItsaStatusesJson
      }
    }

    "the full Json is present" should {
      "correctly read JSON to a model" in {
        itsaStatusesReadJson.as[ItsaStatuses] shouldBe itsaStatuses
      }
    }

    "the minimal Json is present" should {
      "correctly read JSON to a model" in {
        minimalItsaStatusesJson.as[ItsaStatuses] shouldBe minimalItsaStatuses
      }
    }
  }

}
