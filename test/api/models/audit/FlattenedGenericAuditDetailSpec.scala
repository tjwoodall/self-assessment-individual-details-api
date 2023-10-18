/*
 * Copyright 2023 HM Revenue & Customs
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

package api.models.audit

import api.models.auth.UserDetails
import api.models.errors.TaxYearFormatError
import config.MockAppConfig
import play.api.http.Status.{BAD_REQUEST, OK}
import play.api.libs.json.{JsValue, Json}
import support.UnitSpec
import v1.models.domain.StatusEnum.{`MTD Mandated`, `No Status`}
import v1.models.domain.StatusReasonEnum.{`ITSA Q4 declaration`, `Sign up - return available`}
import v1.models.response.{ItsaStatusDetails, ItsaStatuses, RetrieveItsaStatusResponse}

class FlattenedGenericAuditDetailSpec extends UnitSpec with MockAppConfig {

  val versionNumber: String = "1.0"
  val nino: String = "XX751130C"
  val taxYear: String = "2021-22"
  val agentReferenceNumber: Option[String] = Some("012345678")
  val userType: String = "Agent"
  val userDetails: UserDetails = UserDetails("mtdId", userType, agentReferenceNumber)
  val correlationId: String = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"
  val successResponse = RetrieveItsaStatusResponse(itsaStatuses = List(
    ItsaStatuses(
      "2021-22",
      Some(
        List(
          ItsaStatusDetails("2023-06-01T10:19:00.303Z", `No Status`, `Sign up - return available`, Some(BigDecimal("99999999999.99")))
        ))),
    ItsaStatuses(
      "2020-21",
      Some(
        List(
          ItsaStatusDetails("2022-05-01T10:19:00.101Z", `MTD Mandated`, `ITSA Q4 declaration`, Some(BigDecimal("8.88")))
        ))),
    ItsaStatuses("2019-20", Some(Nil)),
    ItsaStatuses("2018-19", None)
  ))
  val itsaStatusesJson =
    s"""
       |[{
       |		"taxYear": "2021-22",
       |		"itsaStatusDetails": [{
       |			"submittedOn": "2023-06-01T10:19:00.303Z",
       |			"status": "No Status",
       |			"statusReason": "Sign up - return available",
       |			"businessIncome2YearsPrior": 99999999999.99
       |		}]
       |	}, {
       |		"taxYear": "2020-21",
       |		"itsaStatusDetails": [{
       |			"submittedOn": "2022-05-01T10:19:00.101Z",
       |			"status": "MTD Mandated",
       |			"statusReason": "ITSA Q4 declaration",
       |			"businessIncome2YearsPrior": 8.88
       |		}]
       |	}, {
       |		"taxYear": "2019-20",
       |		"itsaStatusDetails": []
       |	}, {
       |		"taxYear": "2018-19"
       |	}
       |]""".stripMargin

  val auditDetailJsonSuccess: JsValue = Json.parse(
    s"""
       |{
       |    "versionNumber": "$versionNumber",
       |    "userType": "$userType",
       |    "agentReferenceNumber": "${agentReferenceNumber.get}",
       |    "nino": "$nino",
       |    "taxYear": "$taxYear",
       |    "futureYears": true,
       |    "history": true,
       |    "itsaStatuses": $itsaStatusesJson,
       |    "X-CorrelationId": "$correlationId",
       |    "outcome": "success",
       |    "httpStatusCode": $OK
       |}
    """.stripMargin
  )


  val auditDetailModelSuccess: FlattenedGenericAuditDetail = FlattenedGenericAuditDetail(
    versionNumber = Some(versionNumber),
    userDetails = userDetails,
    params = Map("nino" -> nino, "taxYear" -> taxYear),
    futureYears = Some("true"),
    history = Some("true"),
    itsaStatuses = Some(Json.toJson(successResponse)),
    `X-CorrelationId` = correlationId,
    auditResponse = AuditResponse(
      httpStatus = OK,
      response = Right(Some(Json.toJson(successResponse)))
    )
  )

  val invalidTaxYearAuditDetailJson: JsValue = Json.parse(
    s"""
       |{
       |    "versionNumber": "$versionNumber",
       |    "userType": "$userType",
       |    "agentReferenceNumber": "${agentReferenceNumber.get}",
       |    "nino": "$nino",
       |    "taxYear": "$taxYear",
       |    "futureYears": true,
       |    "history": true,
       |    "X-CorrelationId": "$correlationId",
       |    "outcome": "error",
       |    "httpStatusCode": $BAD_REQUEST,
       |    "errorCodes": ["FORMAT_TAX_YEAR"]
       |}
    """.stripMargin
  )

  val invalidTaxYearAuditDetailModel: FlattenedGenericAuditDetail = FlattenedGenericAuditDetail(
    versionNumber = Some(versionNumber),
    userDetails = userDetails,
    params = Map("nino" -> nino, "taxYear" -> taxYear),
    futureYears = Some("true"),
    history = Some("true"),
    itsaStatuses = None,
    `X-CorrelationId` = correlationId,
    auditResponse = AuditResponse(
      httpStatus = BAD_REQUEST,
      response = Left(List(AuditError(TaxYearFormatError.code)))
    )
  )

  "FlattenedGenericAuditDetail" when {
    "written to JSON (success)" should {
      "produce the expected JsObject" in {
        Json.toJson(auditDetailModelSuccess) shouldBe auditDetailJsonSuccess
      }
    }

    "written to JSON (error)" should {
      "produce the expected JsObject" in {
        Json.toJson(invalidTaxYearAuditDetailModel) shouldBe invalidTaxYearAuditDetailJson
      }
    }
  }

}
