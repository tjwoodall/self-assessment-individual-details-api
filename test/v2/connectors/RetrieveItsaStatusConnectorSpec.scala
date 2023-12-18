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

package v2.connectors

import api.connectors.ConnectorSpec
import api.models.domain.{Nino, TaxYear}
import api.models.outcomes.ResponseWrapper
import v2.models.domain.{StatusEnum, StatusReasonEnum}
import v2.models.request.RetrieveItsaStatusRequestData
import v2.models.response.{ItsaStatusDetails, ItsaStatuses, RetrieveItsaStatusResponse}

import scala.concurrent.Future

class RetrieveItsaStatusConnectorSpec extends ConnectorSpec {

  private val nino    = "AA111111A"
  private val taxYear = TaxYear.fromMtd("2023-24")

  "RetrieveItsaStatusConnector" should {
    "return a 200 status and expected response for a success scenario" in new IfsTest with Test {

      willGet(url = s"$baseUrl/income-tax/$nino/person-itd/itsa-status/${taxYear.asTysDownstream}?futureYears=true&history=true")
        .returns(Future.successful(outcome))

      await(connector.retrieve(request)) shouldBe outcome
    }
  }

  trait Test {
    _: ConnectorTest =>

    val connector: RetrieveItsaStatusConnector = new RetrieveItsaStatusConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    val request: RetrieveItsaStatusRequestData = RetrieveItsaStatusRequestData(Nino(nino), taxYear, futureYears = true, history = true)

    val itsaStatusDetails: ItsaStatusDetails = ItsaStatusDetails(
      submittedOn = "2023-05-23T12:29:27.566Z",
      status = StatusEnum.`No Status`,
      statusReason = StatusReasonEnum.`Sign up - return available`,
      businessIncome2YearsPrior = Some(23600.99)
    )

    val itsaStatuses: ItsaStatuses = ItsaStatuses(
      taxYear = taxYear.asMtd,
      itsaStatusDetails = Some(Seq(itsaStatusDetails))
    )

    val responseModel: RetrieveItsaStatusResponse = RetrieveItsaStatusResponse(
      itsaStatuses = Seq(itsaStatuses)
    )

    val outcome: Right[Nothing, ResponseWrapper[RetrieveItsaStatusResponse]] = Right(ResponseWrapper(correlationId, responseModel))
  }

}
