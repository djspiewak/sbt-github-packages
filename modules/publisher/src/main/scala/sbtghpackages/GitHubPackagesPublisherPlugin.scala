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

object GitHubPackagesPublisherPlugin extends AutoPlugin {
  @volatile
  private[this] var alreadyWarned = false

  override def requires = plugins.JvmPlugin
  override def trigger = allRequirements

  object autoImport extends GitHubPackagesPublisherKeys

  import autoImport._

  @deprecated("Remove once `githubOwner` and `githubRepository` keys are eliminated", "0.6")
  private[sbtghpackages] lazy val githubPublishToRepositoryOpt =
    settingKey[Option[GitHubRepository]]("Internal setting for bin-compat. Do not override manually")

  val packagePublishSettings = Seq(
    githubPublishToRepositoryOpt := githubPublishToRepository.?.value,

    credentials ++= githubPublishToRepositoryOpt.value.flatMap(repo => Utils.inferredGitHubCredentials(repo)),

    githubPublishTo := {
      val ms = publishMavenStyle.value
      val back = for {
        repo <- githubPublishToRepositoryOpt.value
      } yield repo.realm at repo.mavenUrl

      back.foreach { _ =>
        if (!ms) {
          sys.error("GitHub Packages does not support Ivy-style publication")
        }
      }

      back
    },

    publishTo := {
      val suppress = githubSuppressPublicationWarning.value
      val log = streams.value.log

      githubPublishTo.value orElse {
        GitHubPackagesPublisherPlugin synchronized {
          if (!alreadyWarned && !suppress) {
            log.warn("undefined key `githubPublishToRepository`")
            log.warn("retaining pre-existing publication settings")
            alreadyWarned = true
          }
        }

        publishTo.value
      }
    },

    publish := {
      val log = streams.value.log
      val repo = githubPublishToRepositoryOpt.value

      repo.foreach { r =>
        if (Utils.inferredGitHubCredentials(r).isEmpty) {
          Utils.reportUnresolvedToken(log, r)
          sys.error(s"Unable to locate a valid GitHub token for ${r.realm} from ${r.tokenSource}")
        }
      }

      update.value
    },

    scmInfo := {
      val back = for {
        repo <- githubPublishToRepositoryOpt.value
      } yield ScmInfo(url(repo.repoUrl), repo.scm)

      back.orElse(scmInfo.value)
    },

    homepage := {
      val back = for {
        repo <- githubPublishToRepositoryOpt.value
      } yield url(repo.repoUrl)

      back.orElse(homepage.value)
    },

    pomIncludeRepository := (_ => false),

    publishMavenStyle := true
  )

  override def projectSettings = packagePublishSettings

  override def buildSettings = Seq(githubSuppressPublicationWarning := false)
}

