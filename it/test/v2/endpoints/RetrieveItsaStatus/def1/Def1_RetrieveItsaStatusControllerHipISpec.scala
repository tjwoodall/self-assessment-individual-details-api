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

package v2.endpoints.RetrieveItsaStatus.def1

import play.api.http.HeaderNames.ACCEPT
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers._
import shared.models.domain.TaxYear
import shared.models.errors._
import shared.services.{AuditStub, AuthStub, DownstreamStub, MtdIdLookupStub}
import shared.support.IntegrationBaseSpec
import v2.models.errors.{FutureYearsFormatError, HistoryFormatError}

class Def1_RetrieveItsaStatusControllerHipISpec extends IntegrationBaseSpec {

  "Calling the 'Retrieve ITSA Status' endpoint" should {
    "return a 200 status code" when {
      "any valid request is made" in new Test {

        override def setupStubs(): Unit =
          DownstreamStub.onSuccess(DownstreamStub.GET, downstreamUri, downstreamQueryParams, OK, downstreamResponse)

        val response: WSResponse = await(request.withQueryStringParameters("futureYears" -> futureYears, "history" -> history).get())
        response.status shouldBe OK
        response.json shouldBe mtdResponse
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }

    "return error according to spec" when {

      "validation error" when {
        def validationErrorTest(requestNino: String,
                                requestTaxYear: String,
                                requestFutureYears: String,
                                requestHistory: String,
                                expectedStatus: Int,
                                expectedBody: MtdError): Unit = {
          s"validation fails with ${expectedBody.code} error" in new Test {

            override val nino: String        = requestNino
            override val mtdTaxYear: String  = requestTaxYear
            override val futureYears: String = requestFutureYears
            override val history: String     = requestHistory

            val response: WSResponse = await(request.withQueryStringParameters("futureYears" -> futureYears, "history" -> history).get())
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
            response.header("Content-Type") shouldBe Some("application/json")
          }
        }

        val input = List(
          ("AA1123A", "2023-24", "true", "true", BAD_REQUEST, NinoFormatError),
          ("AA123456A", "20199", "true", "true", BAD_REQUEST, TaxYearFormatError),
          ("AA123456A", "2023-24", "A", "true", BAD_REQUEST, FutureYearsFormatError),
          ("AA123456A", "2023-24", "true", "B", BAD_REQUEST, HistoryFormatError)
        )
        input.foreach(args => (validationErrorTest _).tupled(args))
      }

      "downstream service error" when {
        def serviceErrorTest(downstreamStatus: Int, downstreamCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"downstream returns a code $downstreamCode error and status $downstreamStatus" in new Test {

            override def setupStubs(): Unit =
              DownstreamStub.onError(DownstreamStub.GET, downstreamUri, downstreamQueryParams, downstreamStatus, errorBody(downstreamCode))

            val response: WSResponse = await(request.withQueryStringParameters("futureYears" -> futureYears, "history" -> history).get())
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
            response.header("Content-Type") shouldBe Some("application/json")
          }
        }

        def errorBody(code: String): String =
          s"""
            |[
            |    {
            |        "errorCode": "$code",
            |        "errorDescription": "error description"
            |    }
            |]
          """.stripMargin

        val input = Seq(
          (BAD_REQUEST, "1215", BAD_REQUEST, NinoFormatError),
          (BAD_REQUEST, "1117", BAD_REQUEST, TaxYearFormatError),
          (BAD_REQUEST, "1216", INTERNAL_SERVER_ERROR, InternalError),
          (NOT_FOUND, "5010", NOT_FOUND, NotFoundError)
        )

        input.foreach(args => (serviceErrorTest _).tupled(args))
      }
    }
  }

  private trait Test {

    val nino: String        = "AA123456A"
    val mtdTaxYear: String  = "2023-24"
    val futureYears: String = "true"
    val history: String     = "true"

    private def downstreamTaxYear: String = TaxYear.fromMtd(mtdTaxYear).asTysDownstream

    def downstreamQueryParams: Map[String, String] = Map(
      "taxYear"     -> downstreamTaxYear,
      "futureYears" -> futureYears,
      "history"     -> history
    )

    val downstreamResponse: JsValue = Json.parse(
      """
        |[
        |  {
        |    "taxYear": "2023-24",
        |    "itsaStatusDetails": [
        |      {
        |        "submittedOn": "2023-05-23T12:29:27.566Z",
        |        "status": "00",
        |        "statusReason": "00",
        |        "businessIncomePriorTo2Years": 23600.99
        |      }
        |    ]
        |  }
        |]
      """.stripMargin
    )

    val mtdResponse: JsValue = Json.parse(
      """
        |{
        |  "itsaStatuses": [
        |    {
        |      "taxYear": "2023-24",
        |      "itsaStatusDetails": [
        |        {
        |          "submittedOn": "2023-05-23T12:29:27.566Z",
        |          "status": "No Status",
        |          "statusReason": "Sign up - return available",
        |          "businessIncome2YearsPrior": 23600.99
        |        }
        |      ]
        |    }
        |  ]
        |}
      """.stripMargin
    )

    def downstreamUri: String = s"/itsd/person-itd/itsa-status/$nino"

    def request: WSRequest = {
      AuditStub.audit()
      AuthStub.authorised()
      MtdIdLookupStub.ninoFound(nino)
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.2.0+json"),
          (AUTHORIZATION, "Bearer 123")
        )
    }

    private def uri: String = s"/itsa-status/$nino/$mtdTaxYear"

    def setupStubs(): Unit = {}

  }

}
