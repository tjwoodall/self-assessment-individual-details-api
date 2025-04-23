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

import play.api.libs.json.{Reads, Writes}
import shared.utils.enums.Enums

sealed trait StatusReasonEnum {
  val fromDownstream: String
}

object StatusReasonEnum {

  implicit val reads: Reads[StatusReasonEnum] =
    Enums.readsFrom[StatusReasonEnum](_.fromDownstream).orElse(Enums.reads[StatusReasonEnum])

  implicit val writes: Writes[StatusReasonEnum] = Enums.writes[StatusReasonEnum]

  case object `Sign up - return available` extends StatusReasonEnum {
    override val fromDownstream: String = "00"
  }

  case object `Sign up - no return available` extends StatusReasonEnum {
    override val fromDownstream: String = "01"
  }

  case object `ITSA final declaration` extends StatusReasonEnum {
    override val fromDownstream: String = "02"
  }

  case object `ITSA Q4 declaration` extends StatusReasonEnum {
    override val fromDownstream: String = "03"
  }

  case object `CESA SA return` extends StatusReasonEnum {
    override val fromDownstream: String = "04"
  }

  case object Complex extends StatusReasonEnum {
    override val fromDownstream: String = "05"
  }

  case object `Ceased income source` extends StatusReasonEnum {
    override val fromDownstream: String = "06"
  }

  case object `Reinstated income source` extends StatusReasonEnum {
    override val fromDownstream: String = "07"
  }

  case object Rollover extends StatusReasonEnum {
    override val fromDownstream: String = "08"
  }

  case object `Income Source Latency Changes` extends StatusReasonEnum {
    override val fromDownstream: String = "09"
  }

  case object `MTD ITSA Opt-Out` extends StatusReasonEnum {
    override val fromDownstream: String = "10"
  }

  case object `MTD ITSA Opt-In` extends StatusReasonEnum {
    override val fromDownstream: String = "11"
  }

  case object `Digitally Exempt` extends StatusReasonEnum {
    override val fromDownstream: String = "12"
  }

}
