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

trait GitHubPackagesKeys {
  val githubOwner = settingKey[String]("The github user or organization name")
  val githubRepository = settingKey[String]("The github repository hosting this package")

  val githubActor = settingKey[String]("The github user to use when authenticating (defaults to github.actor in the git config)")
  val githubTokenSource = settingKey[TokenSource]("Where to get the API token used in publication (defaults to github.token in the git config)")

  val githubSuppressPublicationWarning = settingKey[Boolean]("Whether or not to suppress the publication warning (default: false, meaning the warning will be printed)")
}

object GitHubPackagesKeys extends GitHubPackagesKeys
