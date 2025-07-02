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

package auth

import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import shared.auth.AuthSupportingAgentsAllowedISpec
import shared.services.DownstreamStub

class SelfAssessmentIndividualDetailsApiSupportingAgentsAllowedIfsISpec extends AuthSupportingAgentsAllowedISpec {

  override def servicesConfig: Map[String, Any] =
    Map("feature-switch.ifs_hip_migration_1878.enabled" -> false) ++ super.servicesConfig

  override val callingApiVersion = "2.0"

  override val supportingAgentsAllowedEndpoint = "retrieve-itsa-status"

  override val mtdUrl = s"/itsa-status/$nino/2022-23"

  override def sendMtdRequest(request: WSRequest): WSResponse = await(request.get())

  override val downstreamHttpMethod: DownstreamStub.HTTPMethod = DownstreamStub.GET
  override val downstreamUri: String                           = s"/income-tax/$nino/person-itd/itsa-status/22-23"

  override val maybeDownstreamResponseJson: Option[JsValue] = Some(
    Json.parse(
      """
      |[
      |  {
      |    "taxYear": "2023-24",
      |    "itsaStatusDetails": [
      |      {
      |        "submittedOn": "2023-05-23T12:29:27.566Z",
      |        "status": "No Status",
      |        "statusReason": "Sign up - return available",
      |        "businessIncomePriorTo2Years": 23600.99
      |      }
      |    ]
      |  }
      |]
    """.stripMargin
    ))

}
