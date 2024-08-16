/*
 * Copyright 2022 HM Revenue & Customs
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

package v1.endpoints

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status
import play.api.http.Status.OK
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import shared.services.{AuditStub, AuthStub, DownstreamStub, MtdIdLookupStub}
import shared.support.IntegrationBaseSpec

class AuthISpec extends IntegrationBaseSpec {

  "Calling the sample endpoint" when {

    "MTD ID lookup fails with a 500" should {

      "return 500" in new Test {
        override val nino: String = "AA123456A"

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          MtdIdLookupStub.error(nino, Status.INTERNAL_SERVER_ERROR)
        }

        val response: WSResponse = await(request().get())
        response.status shouldBe Status.INTERNAL_SERVER_ERROR
      }
    }

    "an MTD ID is successfully retrieve from the NINO and the user is authorised" should {
      "MTD ID lookup fails with a 403" should {

        "return 403" in new Test {
          override val nino: String = "AA123456A"

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            MtdIdLookupStub.error(nino, Status.FORBIDDEN)
          }

          val response: WSResponse = await(request().get())
          response.status shouldBe Status.FORBIDDEN
        }
      }
    }

    "return success status" in new Test {

      val downstreamResponse: JsValue = Json.parse(
        """
          |[
          |  {
          |    "taxYear": "2023-24",
          |    "itsaStatusDetails": [
          |      {
          |        "submittedOn": "2023-05-23T12:29:27.566Z",
          |        "status": "No Status",
          |        "statusReason": "Sign up - return available",
          |        "businessIncome2YearsPrior": 23600.99
          |      }
          |    ]
          |  }
          |]
    """.stripMargin
      )

      override def setupStubs(): StubMapping = {
        AuditStub.audit()
        AuthStub.authorised()
        MtdIdLookupStub.ninoFound(nino)
        DownstreamStub.onSuccess(DownstreamStub.GET, downstreamUri, Map("futureYears" -> "false", "history" -> "false"), OK, downstreamResponse)
      }

      val response: WSResponse = await(request().get())
      response.status shouldBe OK
      response.header("Content-Type") shouldBe Some("application/json")
    }
  }

  "MTD ID lookup succeeds but the user is NOT logged in" should {

    "return 403" in new Test {
      override val nino: String = "AA123456A"

      override def setupStubs(): StubMapping = {
        AuditStub.audit()
        MtdIdLookupStub.ninoFound(nino)
        AuthStub.unauthorisedNotLoggedIn()
      }

      val response: WSResponse = await(request().get())
      response.status shouldBe Status.FORBIDDEN
    }
  }

  "MTD ID lookup succeeds but the user is NOT authorised" should {

    "return 403" in new Test {
      override val nino: String = "AA123456A"

      override def setupStubs(): StubMapping = {
        AuditStub.audit()
        MtdIdLookupStub.ninoFound(nino)
        AuthStub.unauthorisedOther()
      }

      val response: WSResponse = await(request().get())
      response.status shouldBe Status.FORBIDDEN
    }
  }

  private trait Test {
    val nino              = "AA123456A"
    val mtdTaxYear        = "2021-22"
    val downstreamTaxYear = "21-22"

    def setupStubs(): StubMapping
    def uri: String = s"/itsa-status/$nino/$mtdTaxYear"

    def downstreamUri: String = s"/income-tax/$nino/person-itd/itsa-status/$downstreamTaxYear"

    def request(): WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.1.0+json"),
          (AUTHORIZATION, "Bearer 123") // some bearer token
        )
    }

  }

}
