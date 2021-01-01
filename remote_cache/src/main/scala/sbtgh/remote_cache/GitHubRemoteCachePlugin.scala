package sbtgh.remote_cache

import sbt._
import sbt.Keys.{aggregate, pushRemoteCacheTo, remoteCacheResolvers, streams}
import sbt.{AutoPlugin, Def, Setting, Task}
import sbtgh.packages.GitHubPackagesKeys

object GitHubRemoteCachePlugin extends AutoPlugin {
  override def requires = sbt.plugins.JvmPlugin

  override def trigger = allRequirements

  object autoImport extends GitHubRemoteCacheKeys with GitHubPackagesKeys {
    type TokenSource = sbtgh.TokenSource
    val TokenSource = sbtgh.TokenSource
  }

  import autoImport._
  import _root_.sbtgh.GitHubResolver._

  override lazy val globalSettings: Seq[Setting[_]] = Seq(
    githubRemoteCacheTokenSource := githubTokenSource.value,
    githubRemoteCacheRepository := "remote-cache",
    githubRemoteCacheMinimum := GitHubRemoteDefaults.minimum,
    githubRemoteCacheTtl := GitHubRemoteDefaults.ttl
  )

  override lazy val buildSettings: Seq[Setting[_]] = Seq(
    githubRemoteCacheCleanOld := packageCleanOldVersionsTask.value,
    githubRemoteCacheCleanOld / aggregate := false
  )

  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    pushRemoteCacheTo := publishToGitHubSetting.value,
    remoteCacheResolvers := {
      val ghOrg = githubRemoteCacheOrganization.value
      val repoName = githubRemoteCacheRepository.value
      List(Resolver.githubPackages(ghOrg, repoName))
    }
  )

  private def publishToGitHubSetting =
    Def.setting {
      val tokenSource = githubRemoteCacheTokenSource.value
      val ghOrg = githubRemoteCacheOrganization.value
      val repoName = githubRemoteCacheRepository.value
      //val context = GitHubCredentialContext.remoteCache(credsFile)
      //github.withRepo(context, Some(ghOrg), repoName, sLog.value) { repo =>
      //  repo.buildRemoteCacheResolver(githubRemoteCachePackage.value, sLog.value)
      //}
      ???
    }

  def packageCleanOldVersionsTask: Def.Initialize[Task[Unit]] =
    Def.task {
      val tokenSource = githubRemoteCacheTokenSource.value
      val ghOrg = githubRemoteCacheOrganization.value
      val repoName = githubRemoteCacheRepository.value
      //val context = GitHubCredentialContext.remoteCache(credsFile)
      val pkg = githubRemoteCachePackage.value
      val s = streams.value
      val min = githubRemoteCacheMinimum.value
      val ttl = githubRemoteCacheTtl.value
      //GitHub.withRepo(context, Some(btyOrg), repoName, s.log) { repo =>
      //  repo.cleandOldVersions(pkg, min, ttl, s.log)
      //}
      ???
    }
}
