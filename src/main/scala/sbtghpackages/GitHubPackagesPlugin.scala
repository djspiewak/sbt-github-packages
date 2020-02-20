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
import scala.util.Try
import scala.util.control.NonFatal

object GitHubPackagesPlugin extends AutoPlugin {
  @volatile
  private[this] var alreadyWarned = false

  override def requires = plugins.JvmPlugin
  override def trigger = allRequirements

  object autoImport extends GitHubPackagesKeys {
    type TokenSource = sbtghpackages.TokenSource
    val TokenSource = sbtghpackages.TokenSource

    implicit class GHPackagesResolverSyntax(val resolver: Resolver.type) extends AnyVal {
      def githubPackages(owner: String, repo: String): MavenRepository =
        realm(owner, repo) at s"https://maven.pkg.github.com/$owner/$repo"
    }
  }

  import autoImport._

  val userDefaults = try {
    val actor = "git config github.actor".!!.trim
    Seq(githubActor := actor)
  } catch {
    case NonFatal(_) =>
      Seq.empty
  }

  val authenticationSettings = Seq(
    githubTokenSource := TokenSource.GitConfig("github.token"),

    credentials += {
      val src = githubTokenSource.value
      inferredGitHubCredentials(githubActor.value, src) match {
        case Some(creds) =>
          creds

        case None =>
          sys.error(s"unable to locate a valid GitHub token from $src")
      }
    })

  val packagePublishSettings = Seq(
    publishTo := {
      val log = streams.value.log
      val back = for {
        owner <- githubOwner.?.value
        repo <- githubRepository.?.value
      } yield "GitHub Package Registry" at s"https://maven.pkg.github.com/$owner/$repo"

      back orElse {
        GitHubPackagesPlugin synchronized {
          if (!alreadyWarned) {
            log.warn("undefined keys `githubOwner` and `githubRepository`")
            log.warn("retaining pre-existing publication settings")
            alreadyWarned = true
          }
        }

        publishTo.value
      }
    },

    scmInfo := {
      for {
        owner <- githubOwner.?.value
        repo <- githubRepository.?.value
      } yield ScmInfo(url(s"https://github.com/$owner/$repo"), s"scm:git@github.com:$owner/$repo")
    },

    homepage := {
      for {
        owner <- githubOwner.?.value
        repo <- githubRepository.?.value
      } yield url(s"https://github.com/$owner/$repo")
    },

    pomIncludeRepository := (_ => false),
    publishMavenStyle := true) ++
    userDefaults ++
    authenticationSettings

  def inferredGitHubCredentials(user: String, tokenSource: TokenSource): Option[Credentials] = {
    def make(tokenM: Option[String]) =
      tokenM map { token =>
        Credentials(
          "GitHub Package Registry",
          "maven.pkg.github.com",
          user,
          token)
      }

    tokenSource match {
      case TokenSource.Or(primary, secondary) =>
        inferredGitHubCredentials(user, primary).orElse(
          inferredGitHubCredentials(user, secondary))

      case TokenSource.Environment(variable) =>
        make(sys.env.get(variable))

      case TokenSource.GitConfig(key) =>
        make(Try(s"git config $key".!!).map(_.trim).toOption)
    }
  }

  private def realm(owner: String, repo: String) =
    s"GitHub Package Registry ($owner/$repo)"

  override def projectSettings = packagePublishSettings
}
