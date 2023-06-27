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

import api.controllers.{AuthorisedController, EndpointLogContext, RequestContext, RequestHandler}
import api.hateoas.HateoasFactory
import api.services.{EnrolmentsAuthService, MtdIdLookupService}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import utils.IdGenerator
import v1.controllers.validators.RetrieveItsaStatusValidator
import v1.models.request.RetrieveItsaStatusRawData
import v1.services.RetrieveItsaStatusService

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class RetrieveItsaStatusController @Inject() (val authService: EnrolmentsAuthService,
                                              val lookupService: MtdIdLookupService,
                                              validator: RetrieveItsaStatusValidator,
                                              service: RetrieveItsaStatusService,
                                              hateoasFactory: HateoasFactory,
                                              cc: ControllerComponents,
                                              val idGenerator: IdGenerator)(implicit ec: ExecutionContext)
    extends AuthorisedController(cc) {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "RetrieveItsaStatusController",
      endpointName = "retrieveItsaStatus"
    )

  def retrieveItsaStatus(nino: String, taxYear: String, futureYears: Option[String], history: Option[String]): Action[AnyContent] =
    authorisedAction(nino).async { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val rawData: RetrieveItsaStatusRawData = RetrieveItsaStatusRawData(
        nino = nino,
        taxYear = taxYear,
        futureYears = futureYears,
        history = history
      )

      val requestHandler =
        RequestHandler
          .withValidator(validator)
          .withService(service.retrieve)
          .withPlainJsonResult()

      requestHandler.handleRequest(rawData)
    }

}
