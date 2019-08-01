/*
 * Copyright 2019 Daniel Spiewak
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

package sbtghpackages

import sbt._, Keys._

import scala.sys.process._
import scala.util.control.NonFatal

object GitHubPackagesPlugin extends AutoPlugin {

  override def requires = plugins.JvmPlugin
  override def trigger = allRequirements

  object autoImport extends GitHubPackagesKeys {
    implicit class GHPackagesResolverSyntax(val resolver: Resolver.type) extends AnyVal {
      def githubPackagesRepo(owner: String, repo: String): MavenRepository =
        s"GitHub $owner Apache Maven Packages" at s"https://maven.pkg.github.com/$owner/$repo"
    }
  }

  import autoImport._

  val userDefaults = try {
    val user = "git config github.user".!!.trim

    if (user != "")
      Seq(githubUser := user)
    else
      Seq.empty
  } catch {
    case NonFatal(_) =>
      Seq.empty
  }

  val packagePublishSettings = Seq(
    githubTokenSource := None,

    credentials ++= inferredGitHubCredentials(
      githubOwner.value,
      githubRepository.value,
      githubUser.value,
      githubTokenSource.value),

    publishTo := Some(
      s"GitHub ${githubOwner.value} Apache Maven Packages" at s"https://maven.pkg.github.com/${githubOwner.value}/${githubRepository.value}"),

    scmInfo := Some(
      ScmInfo(
        url(s"https://github.com/${githubOwner.value}/${githubRepository.value}"),
        s"scm:git@github.com:${githubOwner.value}/${githubRepository.value}")),

    homepage := Some(url(s"https://github.com/${githubOwner.value}/${githubRepository.value}")),

    pomIncludeRepository := (_ => false),
    publishMavenStyle := true) ++
    userDefaults

  def inferredGitHubCredentials(owner: String, repo: String, user: String, tokenSource: Option[TokenSource]) = {
    val tokenM = tokenSource flatMap {
      case TokenSource.Environment(variable) =>
        sys.env.get(variable)

      case TokenSource.GitConfig(key) =>
        Option(s"git config $key".!!).map(_.trim).filterNot(_.isEmpty)
    }

    tokenM map { token =>
      Credentials(
        s"GitHub $owner Apache Maven Packages",
        "maven.pkg.github.com",
        user,
        token)
    }
  }

  override def buildSettings = packagePublishSettings
}
