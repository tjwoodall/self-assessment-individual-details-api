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

import play.api.libs.json.JsValue
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import shared.config.AppConfig
import shared.controllers._
import shared.models.audit.{AuditEvent, AuditResponse, FlattenedGenericAuditDetail}
import shared.models.auth.UserDetails
import shared.models.errors.ErrorWrapper
import shared.routing.Version
import shared.services.{AuditService, EnrolmentsAuthService, MtdIdLookupService}
import shared.utils.IdGenerator
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import v1.controllers.validators.RetrieveItsaStatusValidatorFactory
import v1.services.RetrieveItsaStatusService

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RetrieveItsaStatusController @Inject() (val authService: EnrolmentsAuthService,
                                              val lookupService: MtdIdLookupService,
                                              validatorFactory: RetrieveItsaStatusValidatorFactory,
                                              service: RetrieveItsaStatusService,
                                              auditService: AuditService,
                                              cc: ControllerComponents,
                                              val idGenerator: IdGenerator)(implicit ec: ExecutionContext, appConfig: AppConfig)
    extends AuthorisedController(cc) {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "RetrieveItsaStatusController",
      endpointName = "retrieveItsaStatus"
    )

  def retrieveItsaStatus(nino: String, taxYear: String, futureYears: Option[String], history: Option[String]): Action[AnyContent] =
    authorisedAction(nino).async { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val validator = validatorFactory.validator(nino, taxYear, futureYears, history)

      val requestHandler =
        RequestHandler
          .withValidator(validator)
          .withService(service.retrieve)
          .withAuditing(auditHandler(nino, taxYear, futureYears, history, request))
          .withPlainJsonResult()

      requestHandler.handleRequest()
    }

  private def auditHandler(nino: String,
                           taxYear: String,
                           futureYears: Option[String],
                           history: Option[String],
                           request: UserRequest[AnyContent]): AuditHandler = {
    new AuditHandler() {
      override def performAudit(userDetails: UserDetails, httpStatus: Int, response: Either[ErrorWrapper, Option[JsValue]])(implicit
          ctx: RequestContext,
          ec: ExecutionContext): Unit = {

        val params = Map("nino" -> nino, "taxYear" -> taxYear)

        response match {
          case Left(err: ErrorWrapper) =>
            auditSubmission(
              FlattenedGenericAuditDetail(
                Some(Version(request).name),
                request.userDetails,
                Map("nino" -> nino, "taxYear" -> taxYear),
                futureYears,
                history,
                None,
                ctx.correlationId,
                AuditResponse(httpStatus = httpStatus, response = Left(err.auditErrors))
              ))

          case Right(resp: Option[JsValue]) =>
            auditSubmission(
              FlattenedGenericAuditDetail(
                Some(Version(request).name),
                request.userDetails,
                params,
                futureYears,
                history,
                resp,
                ctx.correlationId,
                AuditResponse(httpStatus = httpStatus, response = Right(None))
              ))
        }
      }
    }
  }

  private def auditSubmission(details: FlattenedGenericAuditDetail)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[AuditResult] = {
    val event = AuditEvent("RetrieveITSAStatus", "Retrieve-ITSA-Status", details)
    auditService.auditEvent(event)
  }

}
