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

package v1.domain

import shared.utils.UnitSpec
import shared.utils.enums.EnumJsonSpecSupport
import v1.models.domain.StatusReasonEnum

class StatusReasonEnumSpec extends UnitSpec with EnumJsonSpecSupport {

  testRoundTrip[StatusReasonEnum](
    ("Sign up - return available", StatusReasonEnum.`Sign up - return available`),
    ("Sign up - no return available", StatusReasonEnum.`Sign up - no return available`),
    ("ITSA final declaration", StatusReasonEnum.`ITSA final declaration`),
    ("ITSA Q4 declaration", StatusReasonEnum.`ITSA Q4 declaration`),
    ("CESA SA return", StatusReasonEnum.`CESA SA return`),
    ("Complex", StatusReasonEnum.Complex),
    ("Ceased income source", StatusReasonEnum.`Ceased income source`),
    ("Reinstated income source", StatusReasonEnum.`Reinstated income source`),
    ("Rollover", StatusReasonEnum.Rollover),
    ("Income Source Latency Changes", StatusReasonEnum.`Income Source Latency Changes`)
  )

}
