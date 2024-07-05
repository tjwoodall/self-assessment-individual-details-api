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

package v1.services

import shared.controllers.EndpointLogContext
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors._
import shared.models.outcomes.ResponseWrapper
import shared.services.ServiceSpec
import uk.gov.hmrc.http.HeaderCarrier
import v1.connectors.MockRetrieveItsaStatusConnector
import v1.models.domain.{StatusEnum, StatusReasonEnum}
import v1.models.errors.{FutureYearsFormatError, HistoryFormatError}
import v1.models.request.RetrieveItsaStatusRequestData
import v1.models.response.{ItsaStatusDetails, ItsaStatuses, RetrieveItsaStatusResponse}

import scala.concurrent.Future

class RetrieveItsaStatusServiceSpec extends ServiceSpec {

  "RetrieveItsaStatusService" should {
    "return correct result for a success" when {
      "a valid request is made" in new Test {
        val outcome: Right[Nothing, ResponseWrapper[RetrieveItsaStatusResponse]] = Right(ResponseWrapper(correlationId, responseModel))

        MockRetrieveItsaStatusConnector
          .retrieve(request)
          .returns(Future.successful(outcome))

        await(service.retrieve(request)) shouldBe outcome
      }

      "map errors according to spec" when {

        def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
          s"a $downstreamErrorCode error is returned from the service" in new Test {

            MockRetrieveItsaStatusConnector
              .retrieve(request)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

            await(service.retrieve(request)) shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val input = List(
          ("INVALID_TAXABLE_ENTITY_ID", NinoFormatError),
          ("INVALID_TAX_YEAR", TaxYearFormatError),
          ("INVALID_FUTURES_YEAR", FutureYearsFormatError),
          ("INVALID_HISTORY", HistoryFormatError),
          ("INVALID_CORRELATION_ID", InternalError),
          ("NOT_FOUND", NotFoundError),
          ("SERVER_ERROR", InternalError),
          ("SERVICE_UNAVAILABLE", InternalError)
        )

        input.foreach(args => (serviceError _).tupled(args))
      }
    }
  }

  trait Test extends MockRetrieveItsaStatusConnector {

    private val nino    = "AA112233A"
    private val taxYear = "2019-20"

    val request: RetrieveItsaStatusRequestData =
      RetrieveItsaStatusRequestData(Nino(nino), TaxYear.fromMtd(taxYear), futureYears = true, history = true)

    val itsaStatusDetails: ItsaStatusDetails = ItsaStatusDetails(
      submittedOn = "2023-05-23T12:29:27.566Z",
      status = StatusEnum.`No Status`,
      statusReason = StatusReasonEnum.`Sign up - return available`,
      businessIncome2YearsPrior = Some(23600.99)
    )

    val itsaStatuses: ItsaStatuses = ItsaStatuses(taxYear, itsaStatusDetails = Some(Seq(itsaStatusDetails)))

    val responseModel: RetrieveItsaStatusResponse = RetrieveItsaStatusResponse(
      itsaStatuses = Seq(itsaStatuses)
    )

    implicit val hc: HeaderCarrier              = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("controller", "RetrieveItsaStatus")

    val service: RetrieveItsaStatusService = new RetrieveItsaStatusService(
      connector = mockRetrieveItsaStatusConnector
    )

  }

}
