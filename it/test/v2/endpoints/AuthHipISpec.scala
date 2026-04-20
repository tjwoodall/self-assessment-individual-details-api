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

package v2.endpoints

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.*
import shared.models.errors.{ClientNotEnrolledError, ClientOrAgentNotAuthorisedError}
import shared.services.{AuditStub, AuthStub, DownstreamStub, EnrolmentAuthStub, MtdIdLookupStub}
import shared.support.IntegrationBaseSpec

class AuthHipISpec extends IntegrationBaseSpec {

  "Calling the sample endpoint" when {

    "MTD ID lookup fails with a 500" should {

      "return 500" in new Test {
        override val nino: String = "AA123456A"

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          MtdIdLookupStub.error(nino, INTERNAL_SERVER_ERROR)
        }

        val response: WSResponse = await(request().get())
        response.status shouldBe INTERNAL_SERVER_ERROR
      }
    }

    "an MTD ID is successfully retrieve from the NINO and the user is authorised" should {
      "MTD ID lookup fails with a 403" should {

        "return 403" in new Test {
          override val nino: String = "AA123456A"

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            MtdIdLookupStub.error(nino, FORBIDDEN)
          }

          val response: WSResponse = await(request().get())
          response.status shouldBe FORBIDDEN
        }
      }
    }

    "return success status" in new Test {

      val downstreamQueryParams: Map[String, String] = Map(
        "taxYear"     -> downstreamTaxYear,
        "futureYears" -> "false",
        "history"     -> "false"
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

      override def setupStubs(): StubMapping = {
        AuditStub.audit()
        AuthStub.authorised()
        MtdIdLookupStub.ninoFound(nino)
        DownstreamStub.onSuccess(DownstreamStub.GET, downstreamUri, downstreamQueryParams, OK, downstreamResponse)
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
      response.status shouldBe FORBIDDEN
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
      response.status shouldBe FORBIDDEN
      response.json shouldBe Json.toJson(ClientOrAgentNotAuthorisedError)
    }
  }

  "User does not have an MTD enrolment" should {

    "return 403 with specific enrolment error" in new Test {
      override val nino: String = "AA123456A"

      private val successfulAuthResponse: JsObject =
        Json.obj(
          "allEnrolments" -> List[String]()
        )

      override def setupStubs(): StubMapping = {
        AuditStub.audit()
        MtdIdLookupStub.ninoFound(nino)
        // First Auth call
        AuthStub
          .insufficientEnrolments()
          .inScenario("AuthFlow")
          .whenScenarioStateIs("Started")
          .willSetStateTo("AfterInsufficient")
          .thenReturn(UNAUTHORIZED, headers = Map("WWW-Authenticate" -> """MDTP detail="InsufficientEnrolments""""))
        // Second Auth call
        AuthStub
          .insufficientEnrolments()
          .inScenario("AuthFlow")
          .whenScenarioStateIs("AfterInsufficient")
          .thenReturn(status = OK, body = successfulAuthResponse)
        // EnrolmentAuthConnector call
        EnrolmentAuthStub.found(NO_CONTENT)
      }

      val response: WSResponse = await(request().get())
      response.status shouldBe FORBIDDEN
      response.json shouldBe Json.toJson(ClientNotEnrolledError)
    }
  }

  private trait Test {
    val nino               = "AA123456A"
    private val mtdTaxYear = "2022-23"
    val downstreamTaxYear  = "22-23"

    def setupStubs(): StubMapping
    private def uri: String = s"/itsa-status/$nino/$mtdTaxYear"

    def downstreamUri: String = s"/itsd/person-itd/itsa-status/$nino"

    def request(): WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.2.0+json"),
          (AUTHORIZATION, "Bearer 123")
        )
    }

  }

}
