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

package v1.controllers.validators

import cats.data.Validated
import cats.data.Validated.Valid
import cats.implicits.catsSyntaxTuple4Semigroupal
import shared.controllers.validators.Validator
import shared.controllers.validators.resolvers.{ResolveBoolean, ResolveNino, ResolveTaxYear}
import shared.models.errors.MtdError
import v1.models.errors.{FutureYearsFormatError, HistoryFormatError}
import v1.models.request.RetrieveItsaStatusRequestData

import javax.inject.Singleton

@Singleton
class RetrieveItsaStatusValidatorFactory {

  private val resolveFutureYears = ResolveBoolean(FutureYearsFormatError)
  private val resolveHistory     = ResolveBoolean(HistoryFormatError)

  def validator(nino: String, taxYear: String, futureYears: Option[String], history: Option[String]): Validator[RetrieveItsaStatusRequestData] =
    new Validator[RetrieveItsaStatusRequestData] {

      def validate: Validated[Seq[MtdError], RetrieveItsaStatusRequestData] =
        (
          ResolveNino(nino),
          ResolveTaxYear(taxYear),
          futureYears match {
            case Some(futureYears) => resolveFutureYears(futureYears)
            case None              => Valid(false)
          },
          history match {
            case Some(history) => resolveHistory(history)
            case None          => Valid(false)
          }
        ).mapN(RetrieveItsaStatusRequestData)

    }

}
