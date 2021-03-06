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

name := "sbt-github-packages"

ThisBuild / baseVersion := "0.5"

ThisBuild / organization := "com.codecommit"
ThisBuild / publishGithubUser := "djspiewak"
ThisBuild / publishFullName := "Daniel Spiewak"

ThisBuild / sbtPlugin := true
ThisBuild / sbtVersion := "1.3.3"

enablePlugins(SbtPlugin)

homepage := Some(url("https://github.com/djspiewak/sbt-github-packages"))

scmInfo := Some(
  ScmInfo(
    url("https://github.com/djspiewak/sbt-github-packages"),
    "scm:git@github.com:djspiewak/sbt-github-packages.git"))

developers := List(
  Developer(id="djspiewak", name="Daniel Spiewak", email="djspiewak@gmail.com", url=url("https://github.com/djspiewak")))

publishMavenStyle := true

scriptedLaunchOpts ++= Seq("-Dplugin.version=" + version.value)
scriptedBufferLog := true
