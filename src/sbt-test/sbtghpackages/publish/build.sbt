import scala.sys.process._

name := "sbt-github-packages-tests-publish"

ThisBuild / organization := "com.codecommit"
ThisBuild / version := "git status -s".!!.trim.substring(0, 7)

ThisBuild / githubOwner := "djspiewak"
ThisBuild / githubRepository := "sbt-github-packages"
ThisBuild / githubTokenSource := Some(TokenSource.Environment("GITHUB_TOKEN"))
