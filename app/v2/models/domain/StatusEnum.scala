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

package v2.models.domain

import play.api.libs.json.Format
import utils.enums.Enums

sealed trait StatusEnum {
  val downstreamValue: String
}

object StatusEnum {
  val parser: PartialFunction[String, StatusEnum] = Enums.parser[StatusEnum]

  case object `No Status` extends StatusEnum {
    val downstreamValue = "No Status"
  }

  case object `MTD Mandated` extends StatusEnum {
    val downstreamValue = "MTD Mandated"
  }

  case object `MTD Voluntary` extends StatusEnum {
    val downstreamValue = "MTD Voluntary"
  }

  case object Annual extends StatusEnum {
    val downstreamValue = "Annual"
  }

  case object `Non Digital` extends StatusEnum {
    val downstreamValue = "Non Digital"
  }

  case object Dormant extends StatusEnum {
    val downstreamValue = "Dormant"
  }

  implicit val format: Format[StatusEnum] = Enums.format[StatusEnum]

  case object `MTD Exempt` extends StatusEnum {
    val downstreamValue = "MTD Exempt"
  }
}
