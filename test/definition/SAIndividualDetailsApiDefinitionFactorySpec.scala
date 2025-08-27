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

import cats.implicits.catsSyntaxValidatedId
import shared.config.Deprecation.NotDeprecated
import shared.config.MockSharedAppConfig
import shared.definition.APIStatus.BETA
import shared.definition.*
import shared.mocks.MockHttpClient
import shared.routing.{Version1, Version2}
import shared.utils.UnitSpec

class SAIndividualDetailsApiDefinitionFactorySpec extends UnitSpec with MockSharedAppConfig {

  class Test extends MockHttpClient with MockSharedAppConfig {
    MockedSharedAppConfig.apiGatewayContext returns "individuals/person"
    val apiDefinitionFactory = new SAIndividualDetailsApiDefinitionFactory(mockSharedAppConfig)
  }

  "definition" when {
    "called" should {
      "return a valid Definition case class" in new Test {
        List(Version1, Version2).foreach { version =>
          MockedSharedAppConfig.apiStatus(version) returns "BETA"
          MockedSharedAppConfig.endpointsEnabled(version).returns(true).anyNumberOfTimes()
          MockedSharedAppConfig.deprecationFor(version).returns(NotDeprecated.valid).anyNumberOfTimes()
        }

        apiDefinitionFactory.definition shouldBe
          Definition(
            api = APIDefinition(
              name = "Self Assessment Individual Details (MTD)",
              description = "An API for retrieving individual details data for Self Assessment",
              context = "individuals/person",
              categories = List("INCOME_TAX_MTD"),
              versions = List(
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

}
