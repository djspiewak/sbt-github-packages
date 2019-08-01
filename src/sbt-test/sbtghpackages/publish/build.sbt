import scala.sys.process._

name := "sbt-github-packages-tests-publish"

ThisBuild / organization := "com.codecommit"
ThisBuild / version := "0.1-SNAPSHOT"

ThisBuild / githubOwner := "djspiewak"
ThisBuild / githubRepository := "sbt-github-packages"
ThisBuild / githubTokenSource := Some(TokenSource.Environment("GITHUB_TOKEN"))
