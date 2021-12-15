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

import sbt.Keys._
import sbt._

object GitHubPackagesResolverPlugin extends AutoPlugin {

  override def requires = plugins.JvmPlugin
  override def trigger = allRequirements

  object autoImport extends GitHubPackagesResolverKeys

  import autoImport._

  val packageResolverSettings = Seq(
    credentials ++= {
      val log = streams.value.log
      val repos = githubRepositories.value

      val (unresolved, credentials) =
        repos.foldLeft((List.empty[GitHubRepository], List.empty[Credentials])) { case ((unresolved, credentials), repo) =>
          Utils.inferredGitHubCredentials(repo) match {
            case Some(creds) => (unresolved, credentials :+ creds)
            case None => (unresolved :+ repo, credentials)
          }
        }

      if (unresolved.nonEmpty) {
        unresolved.foreach(repo => Utils.reportUnresolvedToken(log, repo))

        sys.error(s"Unable to locate valid GitHub tokens for ${unresolved.map(_.realm).mkString(", ")}")
      }

      credentials
    },
    githubRepositories := Nil,
    resolvers ++= githubRepositories.?.value.toSeq.flatten.map(r => r.realm at r.mavenUrl)
  )

  override def projectSettings = packageResolverSettings

}

