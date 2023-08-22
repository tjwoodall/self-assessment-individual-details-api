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

package v1.controllers

import api.controllers.{ControllerBaseSpec, ControllerTestRunner}
import api.models.domain.TaxYear
import api.models.errors.{ErrorWrapper, NinoFormatError}
import api.models.outcomes.ResponseWrapper
import config.MockAppConfig
import play.api.libs.json.Json
import play.api.mvc.Result
import v1.controllers.validators.MockRetrieveItsaStatusValidatorFactory
import v1.models.domain.{StatusEnum, StatusReasonEnum}
import v1.models.errors.FutureYearsFormatError
import v1.models.request.RetrieveItsaStatusRequestData
import v1.models.response.{ItsaStatusDetails, ItsaStatuses, RetrieveItsaStatusResponse}
import v1.services.MockRetrieveItsaStatusService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveItsaStatusControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockRetrieveItsaStatusService
    with MockRetrieveItsaStatusValidatorFactory
    with MockAppConfig {

  private val taxYear = TaxYear.fromMtd("2023-24")

  val requestData: RetrieveItsaStatusRequestData = RetrieveItsaStatusRequestData(
    nino = nino,
    taxYear = taxYear,
    futureYears = false,
    history = false
  )

  val itsaStatusDetails: ItsaStatusDetails = ItsaStatusDetails(
    submittedOn = "2023-05-23T12:29:27.566Z",
    status = StatusEnum.`No Status`,
    statusReason = StatusReasonEnum.`Sign up - return available`,
    businessIncomePriorTo2Years = Some(23600.99)
  )

  val itsaStatuses: ItsaStatuses = ItsaStatuses(
    taxYear = taxYear.asMtd,
    itsaStatusDetails = Some(List(itsaStatusDetails))
  )

  val responseModel: RetrieveItsaStatusResponse = RetrieveItsaStatusResponse(
    itsaStatuses = List(itsaStatuses)
  )

  private val mtdResponse = Json.parse(
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
      |          "businessIncomePriorTo2Years": 23600.99
      |        }
      |      ]
      |    }
      |  ]
      |}
    """.stripMargin
  )

  "retrieve" should {
    "return OK" when {
      "the request is valid" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockRetrieveItsaStatusService
          .retrieve(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseModel))))

        runOkTest(expectedStatus = OK, maybeExpectedResponseBody = Some(mtdResponse))
      }
    }

    "return the error as per spec" when {
      "the validation fails" in new Test {
        willUseValidator(returning(NinoFormatError))
        runErrorTest(NinoFormatError)
      }

      "the service returns an error" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockRetrieveItsaStatusService
          .retrieve(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, FutureYearsFormatError))))

        runErrorTest(FutureYearsFormatError)
      }
    }
  }

  trait Test extends ControllerTest {

    val controller = new RetrieveItsaStatusController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockRetrieveItsaStatusValidatorFactory,
      service = mockRetrieveItsaStatusService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = controller.retrieveItsaStatus(nino.nino, taxYear.asMtd, None, None)(fakeGetRequest)
  }

}
