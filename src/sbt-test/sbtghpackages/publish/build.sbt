import scala.sys.process._

name := "sbt-github-packages-tests-publish"
organization := "com.codecommit"

version := "git status -s".!!.trim.substring(0, 7)

githubOwner := "djspiewak"
githubRepository := "sbt-github-packages"
githubTokenSource := Some(TokenSource.Environment("GITHUB_TOKEN"))
