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

package v1.models.domain

import play.api.libs.json.Format
import utils.enums.Enums

sealed trait StatusReasonEnum

object StatusReasonEnum {
  val parser: PartialFunction[String, StatusReasonEnum] = Enums.parser[StatusReasonEnum]
  implicit val format: Format[StatusReasonEnum]         = Enums.format[StatusReasonEnum]

  case object `Sign up - return available` extends StatusReasonEnum

  case object `Sign up - no return available` extends StatusReasonEnum

  case object `ITSA final declaration` extends StatusReasonEnum

  case object `ITSA Q4 declaration` extends StatusReasonEnum

  case object `CESA SA return` extends StatusReasonEnum

  case object Complex extends StatusReasonEnum

  case object `Ceased income source` extends StatusReasonEnum

  case object `Reinstated income source` extends StatusReasonEnum

  case object Rollover extends StatusReasonEnum

  case object `Income Source Latency Changes` extends StatusReasonEnum

}
