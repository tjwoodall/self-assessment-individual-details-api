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

package definition

import api.connectors.MockHttpClient
import cats.implicits.catsSyntaxValidatedId
import config.Deprecation.NotDeprecated
import config.{ConfidenceLevelConfig, MockAppConfig}
import definition.APIStatus.{ALPHA, BETA}
import routing.{Version1, Version2}
import support.UnitSpec
import uk.gov.hmrc.auth.core.ConfidenceLevel

class ApiDefinitionFactorySpec extends UnitSpec with MockAppConfig {

  private val confidenceLevel: ConfidenceLevel = ConfidenceLevel.L200

  class Test extends MockHttpClient with MockAppConfig {
    val apiDefinitionFactory = new ApiDefinitionFactory(mockAppConfig)
    MockAppConfig.apiGatewayContext returns "individuals/person"
  }

  "definition" when {
    "called" should {
      "return a valid Definition case class when confidence level 200 checking is enforced" in {
        testDefinitionWithConfidence(ConfidenceLevelConfig(confidenceLevel = confidenceLevel, definitionEnabled = true, authValidationEnabled = true))
      }

      "return a valid Definition case class when confidence level checking 50 is enforced" in {
        testDefinitionWithConfidence(
          ConfidenceLevelConfig(confidenceLevel = confidenceLevel, definitionEnabled = false, authValidationEnabled = false))
      }

      def testDefinitionWithConfidence(confidenceLevelConfig: ConfidenceLevelConfig): Unit = new Test {

        List(
          (Version1, BETA),
          (Version2, BETA)
        ).foreach { case (version, status) =>
          MockAppConfig.apiStatus(version) returns status.toString
          MockAppConfig.endpointsEnabled(version) returns true
          MockAppConfig
            .deprecationFor(version)
            .returns(NotDeprecated.valid)
            .anyNumberOfTimes()
        }
        MockAppConfig.confidenceLevelCheckEnabled.returns(confidenceLevelConfig).anyNumberOfTimes()

        private val readScope                = "read:self-assessment"
        private val writeScope               = "write:self-assessment"
        val confidenceLevel: ConfidenceLevel = if (confidenceLevelConfig.authValidationEnabled) ConfidenceLevel.L200 else ConfidenceLevel.L50

        apiDefinitionFactory.definition shouldBe
          Definition(
            scopes = List(
              Scope(
                key = readScope,
                name = "View your Self Assessment information",
                description = "Allow read access to self assessment data",
                confidenceLevel
              ),
              Scope(
                key = writeScope,
                name = "Change your Self Assessment information",
                description = "Allow write access to self assessment data",
                confidenceLevel
              )
            ),
            api = APIDefinition(
              name = "Self Assessment Individual Details (MTD)",
              description = "An API for retrieving individual details data for Self Assessment",
              context = "individuals/person",
              categories = List("INCOME_TAX_MTD"),
              versions = List(
                APIVersion(
                  Version1,
                  status = BETA,
                  endpointsEnabled = true
                ),
                APIVersion(
                  Version2,
                  status = BETA,
                  endpointsEnabled = true
                )
              ),
              requiresTrust = None
            )
          )
      }
    }
  }

  "confidenceLevel" when {
    List(
      (true, ConfidenceLevel.L250, ConfidenceLevel.L250),
      (true, ConfidenceLevel.L200, ConfidenceLevel.L200),
      (false, ConfidenceLevel.L200, ConfidenceLevel.L50)
    ).foreach { case (definitionEnabled, configCL, expectedDefinitionCL) =>
      s"confidence-level-check.definition.enabled is $definitionEnabled and confidence-level = $configCL" should {
        s"return confidence level $expectedDefinitionCL" in new Test {
          MockAppConfig.confidenceLevelCheckEnabled returns ConfidenceLevelConfig(
            confidenceLevel = configCL,
            definitionEnabled = definitionEnabled,
            authValidationEnabled = true)
          apiDefinitionFactory.confidenceLevel shouldBe expectedDefinitionCL
        }
      }
    }
  }

  "buildAPIStatus" when {
    "the 'apiStatus' parameter is present and valid" should {
      List(
        (Version1, BETA),
        (Version2, BETA)
      ).foreach { case (version, status) =>
        s"return the correct $status for $version" in new Test {
          MockAppConfig.apiStatus(version) returns status.toString
          MockAppConfig
            .deprecationFor(version)
            .returns(NotDeprecated.valid)
            .anyNumberOfTimes()
          apiDefinitionFactory.buildAPIStatus(version) shouldBe status
        }
      }
    }

    "the 'apiStatus' parameter is present and invalid" should {
      List(Version1, Version2).foreach { version =>
        s"default to alpha for $version " in new Test {
          MockAppConfig.apiStatus(version) returns "ALPHO"
          MockAppConfig
            .deprecationFor(version)
            .returns(NotDeprecated.valid)
            .anyNumberOfTimes()
          apiDefinitionFactory.buildAPIStatus(version) shouldBe ALPHA
        }
      }
    }
  }

}
