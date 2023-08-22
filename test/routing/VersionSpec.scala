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

package routing

import play.api.http.HeaderNames.ACCEPT
import play.api.libs.json.{JsError, JsPath, JsResult, Json}
import play.api.test.FakeRequest
import routing.Version.versionFormat
import support.UnitSpec

class VersionSpec extends UnitSpec {

  "Versions" when {

    "serialized to Json" must {
      "return the expected Json output" in {
        val version: Version = Version1
        val expected         = Json.parse(""" "1.0" """)
        val result           = Json.toJson(version)
        result shouldBe expected
      }
    }

    "deserialized from a Json array" must {
      "return the expected version enum" in {
        val result = Json.parse(""" [ "1.0" ]""").as[Seq[Version]]
        result shouldBe List(Version1)
      }

      "read from Json containing an invalid version" in {
        val result: JsResult[Seq[Version]] = Json.parse(""" [ "99.0" ]""").validate[Seq[Version]]
        result shouldBe JsError(JsPath(0), "Unrecognised version")
      }
    }

    "retrieved from a request header" must {
      "return an error if the version is unsupported" in {
        val result = Versions.getFromRequest(FakeRequest().withHeaders((ACCEPT, "application/vnd.hmrc.5.0+json")))
        result shouldBe Left(VersionNotFound)
      }

      "return an error if the Accept header value is invalid" in {
        val result = Versions.getFromRequest(FakeRequest().withHeaders((ACCEPT, s"application/XYZ.${Version1.name}+json")))
        result shouldBe Left(InvalidHeader)
      }

      "return the specified version" in {
        val result = Versions.getFromRequest(FakeRequest().withHeaders((ACCEPT, "application/vnd.hmrc.1.0+json")))
        result shouldBe Right(Version1)
      }
    }
  }

}
