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

import config.SAIndividualDetailsConfig
import play.api.libs.json.*
import shared.models.domain.TaxYear
import shared.utils.enums.Enums

import scala.math.Ordered.orderingToOrdered

enum StatusEnum(val fromDownstream: String) {
  case `No Status`     extends StatusEnum("00")
  case `MTD Mandated`  extends StatusEnum("01")
  case `MTD Voluntary` extends StatusEnum("02")
  case Annual          extends StatusEnum("03")
  // Non Digital will be removed for TY 26-27
  case `Non Digital` extends StatusEnum("04")
  // Update fromDownstream to "04" once Non Digital is removed
  case `Digitally Exempt` extends StatusEnum("")
  case Dormant            extends StatusEnum("05")
  case `MTD Exempt`       extends StatusEnum("99")
}

object StatusEnum {

  given reads(using config: SAIndividualDetailsConfig): Reads[StatusEnum] =
    Enums.readsFrom[StatusEnum](values, _.fromDownstream).orElse(Enums.reads(values)).flatMap {
      case `Non Digital` if TaxYear.currentTaxYear >= TaxYear.ending(config.digitallyExemptTaxYear) => Reads.pure(`Digitally Exempt`)
      case status                                                                                   => Reads.pure(status)
    }

  given Writes[StatusEnum] = Enums.writes[StatusEnum]

}
