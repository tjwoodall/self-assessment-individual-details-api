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

package shared.connectors

import play.api.http.Status.{NOT_FOUND, NO_CONTENT}
import shared.config.MockSharedAppConfig
import shared.mocks.MockHttpClient
import shared.models.errors.{ClientNotEnrolledError, ClientOrAgentNotAuthorisedError, InternalError, MtdError}
import uk.gov.hmrc.http.{HttpResponse, StringContextOps}

import scala.concurrent.Future

class EnrolmentsAuthConnectorSpec extends ConnectorSpec {

  val mtdId = "test-mtdId"

  class Test extends MockHttpClient with MockSharedAppConfig {

    val connector = new EnrolmentsAuthConnector(
      http = mockHttpClient,
      appConfig = mockSharedAppConfig
    )

    MockedSharedAppConfig.enrolmentStoreProxyUrl returns baseUrl
  }

  "getMtdIds" should {
    "return an ClientOrAgentNotAuthorisedError" when {
      "the http client returns a 200" in new Test {
        val statusCode: Int = OK
        MockedHttpClient
          .get[HttpResponse](url"$baseUrl/enrolment-store/enrolments/HMRC-MTD-IT~MTDITID~$mtdId/groups", dummyHeaderCarrierConfig)
          .returns(Future.successful(HttpResponse(statusCode, "")))

        val result: MtdError = await(connector.getMtdIds(mtdId))

        result shouldBe ClientOrAgentNotAuthorisedError
      }

      "the http client returns a 404" in new Test {
        val statusCode: Int = NOT_FOUND
        MockedHttpClient
          .get[HttpResponse](url"$baseUrl/enrolment-store/enrolments/HMRC-MTD-IT~MTDITID~$mtdId/groups", dummyHeaderCarrierConfig)
          .returns(Future.successful(HttpResponse(statusCode, "")))

        val result: MtdError = await(connector.getMtdIds(mtdId))

        result shouldBe ClientOrAgentNotAuthorisedError
      }
    }

    "return an ClientNotEnrolledError" when {
      "the http client returns a 204" in new Test {
        val statusCode: Int = NO_CONTENT
        MockedHttpClient
          .get[HttpResponse](url"$baseUrl/enrolment-store/enrolments/HMRC-MTD-IT~MTDITID~$mtdId/groups", dummyHeaderCarrierConfig)
          .returns(Future.successful(HttpResponse(statusCode, "")))

        val result: MtdError = await(connector.getMtdIds(mtdId))

        result shouldBe ClientNotEnrolledError
      }
    }
    "return an InternalError" when {
      "the http client returns that error" in new Test {
        val statusCode: Int = IM_A_TEAPOT
        MockedHttpClient
          .get[HttpResponse](url"$baseUrl/enrolment-store/enrolments/HMRC-MTD-IT~MTDITID~$mtdId/groups", dummyHeaderCarrierConfig)
          .returns(Future.successful(HttpResponse(statusCode, "")))

        val result: MtdError = await(connector.getMtdIds(mtdId))

        result shouldBe InternalError
      }
    }
  }

}
