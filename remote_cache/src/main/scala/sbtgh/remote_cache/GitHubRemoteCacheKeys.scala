/*
 * Copyright 2020 Daniel Spiewak
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

package sbtgh.remote_cache

import sbt._
import sbtgh.TokenSource

import scala.concurrent.duration._

trait GitHubRemoteCacheKeys {
  val githubRemoteCacheTokenSource = settingKey[TokenSource](
    "Where to get the API token used in publication (defaults to github.token in the git config)")
  val githubRemoteCacheOrganization = settingKey[String](
    "GitHub organization name to push to")
  val githubRemoteCacheRepository = settingKey[String](
    "GitHub repository to publish to (default: remote-cache)")
  val githubRemoteCachePackage = settingKey[String](
    "GitHub package name")
  val githubRemoteCacheCleanOld = taskKey[Unit](
    "Clean old remote cache")
  val githubRemoteCacheMinimum = settingKey[Int](
    s"Minimum number of cache to keep around (default: ${GitHubRemoteDefaults.minimum})")
  val githubRemoteCacheTtl = settingKey[Duration](
    s"Time to keep remote cache around (default: ${GitHubRemoteDefaults.ttl})")
}

object GitHubRemoteCacheKeys extends GitHubRemoteCacheKeys

object GitHubRemoteDefaults {
  def minimum: Int = 100
  def ttl: Duration = Duration(30, DAYS)
}