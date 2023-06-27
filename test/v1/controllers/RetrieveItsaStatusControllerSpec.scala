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
import api.hateoas.HateoasLinks
import api.mocks.hateoas.MockHateoasFactory
import api.models.domain.{Nino, TaxYear}
import api.models.errors.{ErrorWrapper, FutureYearsFormatError, NinoFormatError}
import api.models.outcomes.ResponseWrapper
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import v1.mocks.services.MockRetrieveItsaStatusService
import v1.mocks.validators.MockRetrieveItsaStatusValidator
import v1.models.domain.{StatusEnum, StatusReasonEnum}
import v1.models.request.{RetrieveItsaStatusRawData, RetrieveItsaStatusRequest}
import v1.models.response.{ItsaStatusDetails, ItsaStatuses, RetrieveItsaStatusResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveItsaStatusControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockRetrieveItsaStatusService
    with MockHateoasFactory
    with MockRetrieveItsaStatusValidator
    with HateoasLinks {

  val taxYear: String = "2023-24"

  val rawData: RetrieveItsaStatusRawData = RetrieveItsaStatusRawData(
    nino = nino,
    taxYear = taxYear,
    futureYears = None,
    history = None
  )

  val requestData: RetrieveItsaStatusRequest = RetrieveItsaStatusRequest(
    nino = Nino(nino),
    taxYear = TaxYear.fromMtd(taxYear),
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
    taxYear = taxYear,
    itsaStatusDetails = Some(Seq(itsaStatusDetails))
  )

  val responseModel: RetrieveItsaStatusResponse = RetrieveItsaStatusResponse(
    itsaStatuses = Seq(itsaStatuses)
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
      |          "businessIncomePriorTo2Years": 23600.99
      |        }
      |      ]
      |    }
      |  ]
      |}
    """.stripMargin
  )

  "RetrieveItsaStatusController" should {
    "return OK" when {
      "a valid request is made" in new Test {
        MockedRetrieveItsaStatusValidator
          .parseAndValidateRequest(rawData)
          .returns(Right(requestData))

        MockRetrieveItsaStatusService
          .retrieve(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseModel))))

        runOkTest(expectedStatus = OK, maybeExpectedResponseBody = Some(mtdResponse))
      }
    }

    "return the error as per spec" when {
      "the validation fails" in new Test {
        MockedRetrieveItsaStatusValidator
          .parseAndValidateRequest(rawData)
          .returns(Left(ErrorWrapper(correlationId, NinoFormatError, None)))

        runErrorTest(NinoFormatError)
      }

      "the service returns an error" in new Test {
        MockedRetrieveItsaStatusValidator
          .parseAndValidateRequest(rawData)
          .returns(Right(requestData))

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
      validator = mockRetrieveItsaStatusValidator,
      service = mockRetrieveItsaStatusService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = controller.retrieveItsaStatus(nino, taxYear, None, None)(fakeGetRequest)

  }

}
