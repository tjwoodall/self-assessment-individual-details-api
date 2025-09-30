/*
 * Copyright 2024 HM Revenue & Customs
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

package v2.retrieveItsaStatus.model.response

import config.SAIndividualDetailsConfig
import play.api.libs.json.{JsObject, Json, OWrites, Reads}
import shared.utils.JsonWritesUtil
import v2.retrieveItsaStatus.def1.model.response.ItsaStatuses

trait RetrieveItsaStatusResponse

object RetrieveItsaStatusResponse extends JsonWritesUtil {

  implicit val writes: OWrites[RetrieveItsaStatusResponse] = writesFrom { case def1: Def1_RetrieveItsaStatusResponse =>
    Json.toJson(def1).as[JsObject]

  }

}

case class Def1_RetrieveItsaStatusResponse(itsaStatuses: Seq[ItsaStatuses]) extends RetrieveItsaStatusResponse

object Def1_RetrieveItsaStatusResponse {

  implicit def reads(implicit config: SAIndividualDetailsConfig): Reads[Def1_RetrieveItsaStatusResponse] = json =>
    json
      .validate[Seq[ItsaStatuses]]
      .map(itsaStatuses => Def1_RetrieveItsaStatusResponse(itsaStatuses))

  implicit val writes: OWrites[Def1_RetrieveItsaStatusResponse] = Json.writes[Def1_RetrieveItsaStatusResponse]

}
