import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt._

object AppDependencies {

  private val bootstrapPlayVersion = "7.15.0"

  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc"                  %% "bootstrap-backend-play-28" % bootstrapPlayVersion,
    "org.typelevel"                %% "cats-core"                 % "2.9.0",
    "com.chuusai"                  %% "shapeless"                 % "2.4.0-M1",
    "com.fasterxml.jackson.module" %% "jackson-module-scala"      % "2.14.2"
  )

  val test: Seq[ModuleID] = Seq(
    "org.scalatest"         %% "scalatest"              % "3.2.15"             % "test, it",
    "org.scalacheck"        %% "scalacheck"             % "1.17.0"             % "test, it",
    "com.vladsch.flexmark"   % "flexmark-all"           % "0.64.6"             % "test, it",
    "org.scalamock"         %% "scalamock"              % "5.2.0"              % "test, it",
    "com.typesafe.play"     %% "play-test"              % PlayVersion.current  % "test, it",
    "uk.gov.hmrc"           %% "bootstrap-test-play-28" % bootstrapPlayVersion % "test, it",
    "com.github.tomakehurst" % "wiremock-jre8"          % "2.35.0"             % "test, it",
    "io.swagger.parser.v3"   % "swagger-parser-v3"      % "2.1.12"             % "test, it"
  )

}
