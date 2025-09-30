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

import config.SAIndividualDetailsConfig
import shared.config.{ConfigFeatureSwitches, SharedAppConfig}
import shared.connectors.DownstreamUri.{HipUri, IfsUri}
import shared.connectors.httpparsers.StandardDownstreamHttpParser.reads
import shared.connectors.{BaseDownstreamConnector, DownstreamOutcome, DownstreamUri}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2
import v2.retrieveItsaStatus.def1.model.request.Def1_RetrieveItsaStatusRequestData
import v2.retrieveItsaStatus.model.request.RetrieveItsaStatusRequestData
import v2.retrieveItsaStatus.model.response.{Def1_RetrieveItsaStatusResponse, RetrieveItsaStatusResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RetrieveItsaStatusConnector @Inject() (val http: HttpClientV2, val appConfig: SharedAppConfig)(implicit config: SAIndividualDetailsConfig)
    extends BaseDownstreamConnector {

  def retrieve(request: RetrieveItsaStatusRequestData)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[RetrieveItsaStatusResponse]] = {

    request match {
      case def1: Def1_RetrieveItsaStatusRequestData =>
        import request.*

        val downstreamUri: DownstreamUri[Def1_RetrieveItsaStatusResponse] = if (ConfigFeatureSwitches().isEnabled("ifs_hip_migration_1878")) {
          HipUri[Def1_RetrieveItsaStatusResponse](
            s"itsd/person-itd/itsa-status/$nino?taxYear=${taxYear.asTysDownstream}&futureYears=$futureYears&history=$history"
          )
        } else {
          IfsUri[Def1_RetrieveItsaStatusResponse](
            s"income-tax/$nino/person-itd/itsa-status/${taxYear.asTysDownstream}?futureYears=$futureYears&history=$history"
          )
        }

        get(downstreamUri)
    }

  }

}
