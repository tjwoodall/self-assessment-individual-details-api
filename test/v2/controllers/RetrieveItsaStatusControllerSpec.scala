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

package v2.controllers

import config.MockSAIndividualDetailsConfig
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import shared.controllers.{ControllerBaseSpec, ControllerTestRunner}
import shared.models.audit.{AuditEvent, AuditResponse, FlattenedGenericAuditDetail}
import shared.models.auth.UserDetails
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors.{ErrorWrapper, NinoFormatError}
import shared.models.outcomes.ResponseWrapper
import shared.services.MockAuditService
import v2.controllers.validators.MockRetrieveItsaStatusValidatorFactory
import v2.models.domain.StatusEnum.`No Status`
import v2.models.domain.StatusReasonEnum.`Sign up - return available`
import v2.models.errors.FutureYearsFormatError
import v2.models.request.RetrieveItsaStatusRequestData
import v2.models.response.{ItsaStatusDetails, ItsaStatuses, RetrieveItsaStatusResponse}
import v2.services.MockRetrieveItsaStatusService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveItsaStatusControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockRetrieveItsaStatusService
    with MockAuditService
    with MockRetrieveItsaStatusValidatorFactory
    with MockSAIndividualDetailsConfig {

  private val nino        = Nino("AA123456A")
  private val taxYear     = TaxYear.fromMtd("2023-24")
  private val userType    = "Individual"
  private val userDetails = UserDetails("mtdId", userType, None)
  private val requestData = RetrieveItsaStatusRequestData(nino, taxYear, futureYears = false, history = false)

  private val itsaStatusDetails = ItsaStatusDetails("2023-05-23T12:29:27.566Z", `No Status`, `Sign up - return available`, Some(23600.99))
  private val itsaStatuses      = ItsaStatuses(taxYear.asMtd, Some(List(itsaStatusDetails)))
  private val response          = RetrieveItsaStatusResponse(List(itsaStatuses))

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
      |          "businessIncome2YearsPrior": 23600.99
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

        MockedRetrieveItsaStatusService
          .retrieve(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, response))))

        runOkTestWithAudit(expectedStatus = OK, maybeExpectedResponseBody = Some(mtdResponse))
      }
    }

    "return the error as per spec" when {
      "the validation fails" in new Test {
        willUseValidator(returning(NinoFormatError))

        runErrorTestWithAudit(NinoFormatError)
      }

      "the service returns an error" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockedRetrieveItsaStatusService
          .retrieve(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, FutureYearsFormatError))))

        runErrorTestWithAudit(FutureYearsFormatError)
      }
    }
  }

  trait Test extends ControllerTest with AuditEventChecking[FlattenedGenericAuditDetail] {

    private val controller = new RetrieveItsaStatusController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockRetrieveItsaStatusValidatorFactory,
      service = mockRetrieveItsaStatusService,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = controller.retrieveItsaStatus(nino.nino, taxYear.asMtd, None, None)(fakeGetRequest)

    def event(auditResponse: AuditResponse, maybeRequestBody: Option[JsValue]): AuditEvent[FlattenedGenericAuditDetail] =
      AuditEvent(
        auditType = "RetrieveITSAStatus",
        transactionName = "Retrieve-ITSA-Status",
        detail = FlattenedGenericAuditDetail(
          versionNumber = Some(apiVersion.name),
          userDetails = userDetails,
          params = Map("nino" -> nino.toString, "taxYear" -> taxYear.asMtd),
          futureYears = None,
          history = None,
          itsaStatuses = if (auditResponse.errors.isEmpty) Some(mtdResponse) else None,
          `X-CorrelationId` = correlationId,
          auditResponse = auditResponse
        )
      )

  }

}
