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

sealed trait StatusReasonEnum {
  val downstreamValue: String
}

object StatusReasonEnum {

  case object `Sign up - return available` extends StatusReasonEnum {
    val downstreamValue = "Sign up - return available"
  }

  case object signUpNoReturnAvailable extends StatusReasonEnum {
    val downstreamValue = "Sign up - no return available"
  }

  case object itsaFinalDeclaration extends StatusReasonEnum {
    val downstreamValue = "ITSA final declaration"
  }

  case object `ITSA Q4 declaration` extends StatusReasonEnum {
    val downstreamValue = "ITSA Q4 declaration"
  }

  case object cesaSaReturn extends StatusReasonEnum {
    val downstreamValue = "CESA SA return"
  }

  case object complex extends StatusReasonEnum {
    val downstreamValue = "Complex"
  }

  case object ceasedIncomeSource extends StatusReasonEnum {
    val downstreamValue = "Ceased income source"
  }

  case object reinstatedIncomeSource extends StatusReasonEnum {
    val downstreamValue = "Reinstated income source"
  }

  case object rollover extends StatusReasonEnum {
    val downstreamValue = "Rollover"
  }

  implicit val format: Format[StatusReasonEnum]         = Enums.format[StatusReasonEnum]
  val parser: PartialFunction[String, StatusReasonEnum] = Enums.parser[StatusReasonEnum]
}
