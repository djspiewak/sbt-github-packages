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

final class GitHubRepository(
    val owner: String,
    val repository: String,
    val tokenSource: TokenSource
) {
  def realm: String = s"GitHub Package Registry ($owner${if (repository != "_") s"/$repository" else ""})"

  def mavenUrl: String = s"https://maven.pkg.github.com/$owner/$repository"

  def repoUrl: String = s"https://github.com/$owner/$repository"

  def scm: String = s"scm:git@github.com:$owner/$repository.git"
}

object GitHubRepository {

  def apply(owner: String, repository: String, source: TokenSource): GitHubRepository =
    new GitHubRepository(owner, repository, source)

}