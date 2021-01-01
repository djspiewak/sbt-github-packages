package sbtgh

import sbt._

import scala.sys.process._
import scala.util.Try

object GitHubResolver {
  implicit class GitHubResolverSyntax(val resolver: Resolver.type) extends AnyVal {
    def githubPackages(owner: String, repo: String = "_"): MavenRepository =
      realm(owner, repo) at s"https://maven.pkg.github.com/$owner/$repo"
  }

  def resolveTokenSource(tokenSource: TokenSource): Option[String] = {
    tokenSource match {
      case TokenSource.Or(primary, secondary) =>
        resolveTokenSource(primary).orElse(
          resolveTokenSource(secondary))

      case TokenSource.Environment(variable) =>
        sys.env.get(variable)

      case TokenSource.GitConfig(key) =>
        Try(s"git config $key".!!).map(_.trim).toOption
    }
  }

  def inferredGitHubCredentials(user: String, tokenSource: TokenSource): Option[Credentials] = {
    resolveTokenSource(tokenSource) map { token =>
      Credentials(
        "GitHub Package Registry",
        "maven.pkg.github.com",
        user,
        token)
    }
  }

  private def realm(owner: String, repo: String) =
    s"GitHub Package Registry (${owner}${if (repo != "_") s"/$repo" else ""})"
}