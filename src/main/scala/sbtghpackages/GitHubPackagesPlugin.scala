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

import sbt._

object GitHubPackagesPlugin extends AutoPlugin {

  override def requires = GitHubPackagesResolverPlugin && GitHubPackagesPublisherPlugin
  override def trigger = allRequirements

  object autoImport extends GitHubPackagesKeys {
    type TokenSource = sbtghpackages.TokenSource
    val TokenSource = sbtghpackages.TokenSource

    type GitHubRepository = sbtghpackages.GitHubRepository
    val GitHubRepository = sbtghpackages.GitHubRepository

    implicit class GHPackagesResolverSyntax(val resolver: Resolver.type) extends AnyVal {
      def githubPackages(owner: String, repo: String = "_"): MavenRepository =
        realm(owner, repo) at s"https://maven.pkg.github.com/$owner/$repo"
    }
  }

  import autoImport._
  import GitHubPackagesPublisherKeys._
  import GitHubPackagesResolverKeys._
  import GitHubPackagesPublisherPlugin.githubPublishToRepositoryOpt

  val packagePublishSettings = Seq(
    githubTokenSource := TokenSource.Environment("GITHUB_TOKEN") || TokenSource.Property("GITHUB_TOKEN"),
    githubPublishToRepositoryOpt := {
      val source = githubTokenSource.value
      for {
        owner <- githubOwner.?.value
        repo <- githubRepository.?.value
      } yield GitHubRepository(owner, repo, source)
    },
    githubRepositories ++= {
      val source = githubTokenSource.value
      for {
        owner <- githubOwner.?.value
        repo <- githubRepository.?.value
      } yield GitHubRepository(owner, repo, source)
    }
  )

  private def realm(owner: String, repo: String) =
    s"GitHub Package Registry (${owner}${if (repo != "_") s"/$repo" else ""})"

  override def projectSettings = packagePublishSettings

  override def buildSettings = Seq(githubSuppressPublicationWarning := false)
}
