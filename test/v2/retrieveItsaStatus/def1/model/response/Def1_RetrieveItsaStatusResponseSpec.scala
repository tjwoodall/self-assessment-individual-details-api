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

import play.api.libs.json.{JsError, JsObject, JsValue, Json}
import shared.utils.UnitSpec
import v2.models.domain.{StatusEnum, StatusReasonEnum}

class Def1_RetrieveItsaStatusResponseSpec extends UnitSpec {

  private def mtdJson(status: String, statusReason: String): JsValue = Json.parse(
    s"""
      |{
      |  "itsaStatuses": [
      |    {
      |      "taxYear": "2025-26",
      |      "itsaStatusDetails": [
      |        {
      |          "submittedOn": "2025-06-01T10:19:00.303Z",
      |          "status": "$status",
      |          "statusReason": "$statusReason",
      |          "businessIncome2YearsPrior": 99999999999.99
      |        }
      |      ]
      |    }
      |  ]
      |}
    """.stripMargin
  )

  private def downstreamJson(status: String, statusReason: String): JsValue =
    Json.parse(
      s"""
        |[
        |  {
        |    "taxYear": "2025-26",
        |    "itsaStatusDetails": [
        |      {
        |        "submittedOn": "2025-06-01T10:19:00.303Z",
        |        "status": "$status",
        |        "statusReason": "$statusReason",
        |        "businessIncomePriorTo2Years": 99999999999.99
        |      }
        |    ]
        |  }
        |]
      """.stripMargin
    )

  private def model(status: StatusEnum, statusReason: StatusReasonEnum): Def1_RetrieveItsaStatusResponse =
    Def1_RetrieveItsaStatusResponse(
      itsaStatuses = List(
        ItsaStatuses(
          taxYear = "2025-26",
          itsaStatusDetails = Some(
            List(
              ItsaStatusDetails(
                submittedOn = "2025-06-01T10:19:00.303Z",
                status = status,
                statusReason = statusReason,
                businessIncome2YearsPrior = Some(BigDecimal("99999999999.99"))
              )
            )
          )
        )
      )
    )

  private val statusValues: Seq[StatusEnum] = Seq(
    StatusEnum.`No Status`,
    StatusEnum.`MTD Mandated`,
    StatusEnum.`MTD Voluntary`,
    StatusEnum.Annual,
    StatusEnum.`Non Digital`,
    StatusEnum.Dormant,
    StatusEnum.`MTD Exempt`
  )

  private val statusReasonValues: Seq[StatusReasonEnum] = Seq(
    StatusReasonEnum.`Sign up - return available`,
    StatusReasonEnum.`Sign up - no return available`,
    StatusReasonEnum.`ITSA final declaration`,
    StatusReasonEnum.`ITSA Q4 declaration`,
    StatusReasonEnum.`CESA SA return`,
    StatusReasonEnum.Complex,
    StatusReasonEnum.`Ceased income source`,
    StatusReasonEnum.`Reinstated income source`,
    StatusReasonEnum.Rollover,
    StatusReasonEnum.`Income Source Latency Changes`,
    StatusReasonEnum.`MTD ITSA Opt-Out`,
    StatusReasonEnum.`MTD ITSA Opt-In`,
    StatusReasonEnum.`Digitally Exempt`
  )

  "Def1_RetrieveItsaStatusResponse" when {
    "read from a valid JSON" should {
      "produce the expected object when downstream is IFS" in {
        statusValues.foreach { status =>
          statusReasonValues.foreach { statusReason =>
            val json: JsValue = downstreamJson(status.toString, statusReason.toString)

            json.as[Def1_RetrieveItsaStatusResponse] shouldBe model(status, statusReason)
          }
        }
      }

      "produce the expected object when downstream is HIP" in {
        statusValues.foreach { status =>
          statusReasonValues.foreach { statusReason =>
            val json: JsValue = downstreamJson(status.fromDownstream, statusReason.fromDownstream)

            json.as[Def1_RetrieveItsaStatusResponse] shouldBe model(status, statusReason)
          }
        }
      }
    }

    "read from empty JSON" should {
      "produce a JsError" in {
        val invalidJson: JsObject = JsObject.empty

        invalidJson.validate[Def1_RetrieveItsaStatusResponse] shouldBe a[JsError]
      }
    }

    "written to JSON" should {
      "produce the expected JSON" in {
        statusValues.foreach { status =>
          statusReasonValues.foreach { statusReason =>
            val expectedModel: Def1_RetrieveItsaStatusResponse = model(status, statusReason)

            Json.toJson[Def1_RetrieveItsaStatusResponse](expectedModel) shouldBe mtdJson(
              status.toString,
              statusReason.toString
            )
          }
        }
      }
    }
  }

}
