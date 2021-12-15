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

lazy val `sbt-github-packages` = project
  .in(file("."))
  .enablePlugins(SbtPlugin)
  .settings(
    name := "sbt-github-packages"
  )
  .dependsOn(common, `gh-publisher`, `gh-resolver`)
  .aggregate(common, `gh-publisher`, `gh-resolver`)

lazy val `gh-publisher` = project
  .in(file("modules/publisher"))
  .enablePlugins(SbtPlugin)
  .settings(
    name := "sbt-github-packages-publisher"
  )
  .dependsOn(common)

lazy val `gh-resolver` = project
  .in(file("modules/resolver"))
  .enablePlugins(SbtPlugin)
  .settings(
    name := "sbt-github-packages-resolver"
  )
  .dependsOn(common)

lazy val common = project
  .in(file("modules/common"))
  .settings(
    name := "sbt-github-packages-common"
  )

ThisBuild / baseVersion := "0.5"

ThisBuild / organization := "com.codecommit"
ThisBuild / publishGithubUser := "djspiewak"
ThisBuild / publishFullName := "Daniel Spiewak"

ThisBuild / sbtPlugin := true
ThisBuild / sbtVersion := "1.5.6"

homepage := Some(url("https://github.com/djspiewak/sbt-github-packages"))

scmInfo := Some(
  ScmInfo(
    url("https://github.com/djspiewak/sbt-github-packages"),
    "scm:git@github.com:djspiewak/sbt-github-packages.git"))

developers := List(
  Developer(id="djspiewak", name="Daniel Spiewak", email="djspiewak@gmail.com", url=url("https://github.com/djspiewak")))

publishMavenStyle := true

ThisBuild / scriptedLaunchOpts ++= Seq(
  "-Dplugin.version=" + version.value,
  "-DSTARTUP_PROPERTY_TOKEN=ghp_123321"
)

ThisBuild / scriptedBufferLog := true
