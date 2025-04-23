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

sealed trait StatusEnum {
  val fromDownstream: String
}

object StatusEnum {

  implicit val reads: Reads[StatusEnum] =
    Enums.readsFrom[StatusEnum](_.fromDownstream).orElse(Enums.reads[StatusEnum])

  implicit val writes: Writes[StatusEnum] = Enums.writes[StatusEnum]

  case object `No Status` extends StatusEnum {
    override val fromDownstream: String = "00"
  }

  case object `MTD Mandated` extends StatusEnum {
    override val fromDownstream: String = "01"
  }

  case object `MTD Voluntary` extends StatusEnum {
    override val fromDownstream: String = "02"
  }

  case object Annual extends StatusEnum {
    override val fromDownstream: String = "03"
  }

  case object `Non Digital` extends StatusEnum {
    override val fromDownstream: String = "04"
  }

  case object Dormant extends StatusEnum {
    override val fromDownstream: String = "05"
  }

  case object `MTD Exempt` extends StatusEnum {
    override val fromDownstream: String = "99"
  }

}
