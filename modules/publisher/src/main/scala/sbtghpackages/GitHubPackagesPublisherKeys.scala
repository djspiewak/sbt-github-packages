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

trait GitHubPackagesPublisherKeys {
  val githubPublishToRepository = settingKey[GitHubRepository]("The GitHub repository the package should be published to")
  val githubSuppressPublicationWarning = settingKey[Boolean]("Whether or not to suppress the publication warning (default: false, meaning the warning will be printed)")
  val githubPublishTo = taskKey[Option[Resolver]]("publishTo setting for deploying artifacts to GitHub packages")
}

object GitHubPackagesPublisherKeys extends GitHubPackagesPublisherKeys
