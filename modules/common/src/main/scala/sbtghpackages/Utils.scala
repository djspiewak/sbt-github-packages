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

import sbt.{Credentials, Logger}

import scala.sys.process._
import scala.util.Try

private[sbtghpackages] object Utils {

  def reportUnresolvedToken(log: Logger, repo: GitHubRepository): Unit =
    log.error(
      s"""|Cannot resolve token for ${repo.realm}
          |Make sure at least one token source is configured properly:
          ${helpMessages(repo.tokenSource).map(message => s"|  * $message").mkString("\n")}
          |""".stripMargin
    )

  def inferredGitHubCredentials(repo: GitHubRepository): Option[Credentials] =
    for {
      token <- resolveTokenSource(repo.tokenSource)
    } yield Credentials(repo.realm, "maven.pkg.github.com", "_", token)  // user is ignored by GitHub, so just use "_"

  def resolveTokenSource(tokenSource: TokenSource): Option[String] =
    tokenSource match {
      case TokenSource.Or(primary, secondary) =>
        resolveTokenSource(primary).orElse(resolveTokenSource(secondary))

      case TokenSource.Environment(variable) =>
        sys.env.get(variable)

      case TokenSource.Property(key) =>
        sys.props.get(key)

      case TokenSource.GitConfig(key) =>
        Try(s"git config $key".!!).map(_.trim).toOption
    }

  def helpMessages(tokenSource: TokenSource): List[String] = {
    def loop(input: TokenSource, output: List[String]): List[String] =
      input match {
        case TokenSource.Or(primary, secondary) =>
          loop(primary, Nil) ::: loop(secondary, Nil) ::: output

        case TokenSource.Environment(variable) =>
          s"Use `$variable=<token> sbt` or `export $variable=<token>; sbt`." :: output

        case TokenSource.Property(key) =>
          s"Use `sbt -D$key=<token>` or update `.sbtopts` file." :: output

        case TokenSource.GitConfig(key) =>
          s"Use `git config $key <token>`." :: output
      }

    loop(tokenSource, Nil)
  }

}
