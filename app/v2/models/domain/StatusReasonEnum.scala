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

package v2.models.domain

import play.api.libs.json.*
import shared.utils.enums.Enums

enum StatusReasonEnum(val fromDownstream: String) {
  case `Sign up - return available`    extends StatusReasonEnum("00")
  case `Sign up - no return available` extends StatusReasonEnum("01")
  case `ITSA final declaration`        extends StatusReasonEnum("02")
  case `ITSA Q4 declaration`           extends StatusReasonEnum("03")
  case `CESA SA return`                extends StatusReasonEnum("04")
  case Complex                         extends StatusReasonEnum("05")
  case `Ceased income source`          extends StatusReasonEnum("06")
  case `Reinstated income source`      extends StatusReasonEnum("07")
  case Rollover                        extends StatusReasonEnum("08")
  case `Income Source Latency Changes` extends StatusReasonEnum("09")
  case `MTD ITSA Opt-Out`              extends StatusReasonEnum("10")
  case `MTD ITSA Opt-In`               extends StatusReasonEnum("11")
  case `Digitally Exempt`              extends StatusReasonEnum("12")
}

object StatusReasonEnum {

  given reads: Reads[StatusReasonEnum] = Enums.readsFrom[StatusReasonEnum](values, _.fromDownstream).orElse(Enums.reads(values))

  given Writes[StatusReasonEnum] = Enums.writes[StatusReasonEnum]
}
