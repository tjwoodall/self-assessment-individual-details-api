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
import mocks.MockAppConfig
import support.UnitSpec
import v1.models.request.{RetrieveItsaStatusRawData, RetrieveItsaStatusRequest}

class RetrieveItsaStatusValidatorSpec extends UnitSpec {

  private val validNino    = "AA123456A"
  private val validTaxYear = "2023-24"

  private val invalidNino    = "Darth Sidious"
  private val invalidTaxYear = "23-24"

  private val validRawData                     = RetrieveItsaStatusRawData(validNino, validTaxYear, None, None)
  private val requestWithInvalidNino           = RetrieveItsaStatusRawData(invalidNino, validTaxYear, None, None)
  private val requestWithInvalidTaxYear        = RetrieveItsaStatusRawData(validNino, invalidTaxYear, None, None)
  private val requestWithInvalidNinoAndTaxYear = RetrieveItsaStatusRawData(invalidNino, invalidTaxYear, None, None)
  private val requestWithInvalidFutureYears    = RetrieveItsaStatusRawData(validNino, validTaxYear, Some("yes"), None)
  private val requestWithInvalidHistory        = RetrieveItsaStatusRawData(validNino, validTaxYear, None, Some("yes"))
  private val requestWithAllInvalidRawData     = RetrieveItsaStatusRawData(invalidNino, invalidTaxYear, Some("yes"), Some("yes"))

  class Test extends MockAppConfig {
    implicit val correlationId: String = "1234"

    val validator = new RetrieveItsaStatusValidator()
  }

  "parseAndValidateRequest()" should {
    "return the parsed domain object" when {
      "the request is valid" in new Test {
        val result: Either[ErrorWrapper, RetrieveItsaStatusRequest] =
          validator.parseAndValidateRequest(validRawData)

        result shouldBe Right(RetrieveItsaStatusRequest(Nino(validNino), TaxYear.fromMtd(validTaxYear), futureYears = false, history = false))
      }

      "the request is valid and specifies futureYears and history" in new Test {
        val result: Either[ErrorWrapper, RetrieveItsaStatusRequest] =
          validator.parseAndValidateRequest(validRawData.copy(futureYears = Some("true"), history = Some("true")))

        result shouldBe Right(RetrieveItsaStatusRequest(Nino(validNino), TaxYear.fromMtd(validTaxYear), futureYears = true, history = true))
      }
    }

    "perform the validation and wrap the error in a response wrapper" when {
      "the request has one error" in new Test {
        val result: Either[ErrorWrapper, RetrieveItsaStatusRequest] =
          validator.parseAndValidateRequest(requestWithInvalidNino)

        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }
    }

    "perform the validation and wrap the error in a response wrapper" when {
      "the request has multiple errors caught during preParse" in new Test {
        val result: Either[ErrorWrapper, RetrieveItsaStatusRequest] =
          validator.parseAndValidateRequest(requestWithInvalidNinoAndTaxYear)

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            BadRequestError,
            Some(
              List(
                NinoFormatError,
                TaxYearFormatError
              ))))
      }
    }
  }

  "parseAndValidate()" should {
    "return no errors" when {
      "a valid request is supplied" in new Test {
        val result: Either[Seq[MtdError], RetrieveItsaStatusRequest] =
          validator.parseAndValidate(validRawData)

        result.isRight shouldBe true
      }
    }

    "return errors" when {
      "an invalid nino is supplied" in new Test {
        val result: Either[Seq[MtdError], RetrieveItsaStatusRequest] =
          validator.parseAndValidate(requestWithInvalidNino)

        result shouldBe Left(
          List(
            NinoFormatError
          ))
      }

      "an invalid taxYear is supplied" in new Test {
        val result: Either[Seq[MtdError], RetrieveItsaStatusRequest] =
          validator.parseAndValidate(requestWithInvalidTaxYear)

        result shouldBe Left(
          List(
            TaxYearFormatError
          ))
      }

      "an invalid futureYears value is supplied" in new Test {
        val result: Either[Seq[MtdError], RetrieveItsaStatusRequest] =
          validator.parseAndValidate(requestWithInvalidFutureYears)

        result shouldBe Left(
          List(
            FutureYearsFormatError
          ))
      }

      "an invalid history value is supplied" in new Test {
        val result: Either[Seq[MtdError], RetrieveItsaStatusRequest] =
          validator.parseAndValidate(requestWithInvalidHistory)

        result shouldBe Left(
          List(
            HistoryFormatError
          ))
      }

      "multiple fields are invalid" in new Test {
        val result: Either[Seq[MtdError], RetrieveItsaStatusRequest] =
          validator.parseAndValidate(requestWithAllInvalidRawData)

        result shouldBe Left(
          List(
            NinoFormatError,
            TaxYearFormatError,
            FutureYearsFormatError,
            HistoryFormatError
          ))
      }
    }
  }

}
