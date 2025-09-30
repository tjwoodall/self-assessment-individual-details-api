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

package v2.retrieveItsaStatus

import shared.models.domain.{Nino, TaxYear}
import shared.models.errors.*
import shared.models.outcomes.ResponseWrapper
import shared.services.ServiceSpec
import v2.models.domain.StatusEnum.`No Status`
import v2.models.domain.StatusReasonEnum.`Sign up - return available`
import v2.models.errors.{FutureYearsFormatError, HistoryFormatError}
import v2.retrieveItsaStatus.def1.model.request.Def1_RetrieveItsaStatusRequestData
import v2.retrieveItsaStatus.def1.model.response.{ItsaStatusDetails, ItsaStatuses}
import v2.retrieveItsaStatus.model.response.Def1_RetrieveItsaStatusResponse

import scala.concurrent.Future

class RetrieveItsaStatusServiceSpec extends ServiceSpec with MockRetrieveItsaStatusConnector {

  private val nino    = "AA112233A"
  private val taxYear = "2019-20"
  private val request = Def1_RetrieveItsaStatusRequestData(Nino(nino), TaxYear.fromMtd(taxYear), futureYears = true, history = true)

  private val itsaStatusDetails = ItsaStatusDetails("2023-05-23T12:29:27.566Z", `No Status`, `Sign up - return available`, Some(23600.99))
  private val itsaStatuses      = ItsaStatuses(taxYear, Some(List(itsaStatusDetails)))
  private val responseModel     = Def1_RetrieveItsaStatusResponse(List(itsaStatuses))

  private val service = new RetrieveItsaStatusService(mockRetrieveItsaStatusConnector)

  "RetrieveItsaStatusService" should {
    "return correct result for a success" when {
      "a valid request is made" in {

        val outcome: Right[Nothing, ResponseWrapper[Def1_RetrieveItsaStatusResponse]] = Right(ResponseWrapper(correlationId, responseModel))

        MockedRetrieveItsaStatusConnector
          .retrieve(request)
          .returns(Future.successful(outcome))

        val result = await(service.retrieve(request))

        result shouldBe Right(ResponseWrapper(correlationId, responseModel))
      }

      "map errors according to spec" when {
        def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
          s"a $downstreamErrorCode error is returned from the service" in {

            MockedRetrieveItsaStatusConnector
              .retrieve(request)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

            val result = await(service.retrieve(request))

            result shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val ifsErrors = List(
          ("INVALID_TAXABLE_ENTITY_ID", NinoFormatError),
          ("INVALID_TAX_YEAR", TaxYearFormatError),
          ("INVALID_FUTURES_YEAR", FutureYearsFormatError),
          ("INVALID_HISTORY", HistoryFormatError),
          ("INVALID_CORRELATION_ID", InternalError),
          ("NOT_FOUND", NotFoundError),
          ("SERVER_ERROR", InternalError),
          ("SERVICE_UNAVAILABLE", InternalError)
        )

        val hipErrors = List(
          ("1215", NinoFormatError),
          ("1117", TaxYearFormatError),
          ("1216", InternalError),
          ("5010", NotFoundError)
        )

        (ifsErrors ++ hipErrors).foreach(serviceError.tupled)
      }
    }
  }

}
