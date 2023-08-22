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

import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import support.UnitSpec
import v1.models.errors.{FutureYearsFormatError, HistoryFormatError}
import v1.models.request.RetrieveItsaStatusRequestData

class RetrieveItsaStatusValidatorFactorySpec extends UnitSpec {

  private implicit val correlationId: String = "1234"

  private val validNino    = "AA123456A"
  private val validTaxYear = "2023-24"

  private val parsedNino    = Nino(validNino)
  private val parsedTaxYear = TaxYear.fromMtd(validTaxYear)

  private val invalidNino    = "not-a-nino"
  private val invalidTaxYear = "23-24"

  private val validatorFactory = new RetrieveItsaStatusValidatorFactory

  private def validator(nino: String, taxYear: String, futureYears: Option[String], history: Option[String]) =
    validatorFactory.validator(nino, taxYear, futureYears, history)

  "validator" should {
    "return the parsed domain object" when {
      "passed a valid request" in {
        val result: Either[ErrorWrapper, RetrieveItsaStatusRequestData] =
          validator(validNino, validTaxYear, None, None).validateAndWrapResult()

        result shouldBe Right(
          RetrieveItsaStatusRequestData(parsedNino, parsedTaxYear, futureYears = false, history = false)
        )
      }

      "the request is valid and specifies futureYears and history" in {
        val result: Either[ErrorWrapper, RetrieveItsaStatusRequestData] =
          validator(validNino, validTaxYear, futureYears = Some("true"), history = Some("true")).validateAndWrapResult()

        result shouldBe Right(
          RetrieveItsaStatusRequestData(parsedNino, parsedTaxYear, futureYears = true, history = true)
        )
      }
    }

    "return a single error" when {
      "passed an invalid nino" in {
        val result: Either[ErrorWrapper, RetrieveItsaStatusRequestData] =
          validator(invalidNino, validTaxYear, None, None).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, NinoFormatError)
        )
      }
    }

    "return multiple errors" when {
      "passed multiple invalid fields" in {
        val result: Either[ErrorWrapper, RetrieveItsaStatusRequestData] =
          validator(invalidNino, invalidTaxYear, Some("yes"), Some("no")).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            BadRequestError,
            Some(
              List(NinoFormatError, HistoryFormatError, TaxYearFormatError, FutureYearsFormatError)
            )))
      }
    }
  }

}
