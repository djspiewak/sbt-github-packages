# sbt-github-packages [![Build Status](https://travis-ci.com/djspiewak/sbt-github-packages.svg?branch=master)](https://travis-ci.com/djspiewak/sbt-github-packages)

Configures your project for publication to the [GitHub Package Registry](https://help.github.com/en/articles/about-github-package-registry) using its Apache Maven support. Note that GitHub Packages *exclusively* supports maven-style publication; using Ivy style will result in a warning. Also provides some convenience functionality for *depending* upon artifacts which have been published to the Package Registry.

## Usage

Add the following to your **project/plugins.sbt** file:

```sbt
addSbtPlugin("com.codecommit" % "sbt-github-packages" % "<version>")
```

Published for sbt 1.x. No dependencies.

In order to *publish* artifacts via this plugin, you will need to override the `githubOwner` and `githubRepository` setting keys to the relevant values for your project. For example:

```sbt
githubOwner := "djspiewak"
githubRepository := "sbt-github-packages"
```

In the event that you do *not* override these values, you will see a warning like the following:

```
[warn] undefined keys `githubOwner` and `githubRepository`
[warn] retaining pre-existing publication settings
```

The reason this functionality is disabled by default is you may wish to utilize the `Resolver` syntax (described below) *without* overriding your default publication mechanism. In general, I wouldn't necessarily recommend this, since it means that your publication will not be self-contained within a single artifact realm, but that's a relatively common problem anyway these days so it's probably not a big deal.

As a note on publication, *only* `publishMavenStyle := true` (the default) is supported. If you explicitly override this setting to `false`, the sbt-github-packages plugin will produce an error and refuse to load (unless `githubOwner` and/or `githubRepository` are undefined). The reason for this is to remove a bit of a foot-gun: GitHub Packages will silently allow you to publish Ivy-style packages, and will even show it within the UI, but will not allow you to *resolve* them.

Once everything is configured, run `sbt publish` to publish the package.

### Resolvers

If you're consuming packages that were published in the GitHub Package Registry, this plugin defines some convenience syntax for adding resolvers:

```sbt
resolvers += Resolver.githubPackages("OWNER")
```

You may also *optionally* specify a repository as the second argument. **This is not required!** By default, sbt-github-packages will attempt to resolve from a repository named "_", which does not need to exist. If that repository *does* exist, and it is private, then the token used in authentication must have access to private repositories in that organization. In most cases, just the owner parameter will be sufficient.

This resolver will give you access to packages published on *any* repository within the organization. If the token provided in the authentication information only has access to public repositories, then packages published on private repositories will report "not found". If the token has access to private repositories as well as public, then all packages will be visible.

You will need to ensure that `githubTokenSource` is set to *your* details (i.e. the authentication information for the individual who ran `sbt`). The `TokenSource` ADT has the following possibilities:

```scala
sealed trait TokenSource extends Product with Serializable {
  def ||(that: TokenSource): TokenSource =
    TokenSource.Or(this, that)
}

object TokenSource {
  final case class Environment(variable: String) extends TokenSource
  final case class GitConfig(key: String) extends TokenSource
  final case class Or(primary: TokenSource, secondary: TokenSource) extends TokenSource
}
```

Environment variables are a good default. For example, I have a GitHub token for my laptop stored in the `GITHUB_TOKEN` environment variable. If you mirror this setup, you should configure `githubTokenSource` in the following way:

```sbt
githubTokenSource := TokenSource.Environment("GITHUB_TOKEN")
```

This is, in fact, *exactly* the default configuration. In other words, if you set the `GITHUB_TOKEN` environment variable, then this plugin will work out of the box with no configuration.

To use a token from `~/.gitconfig` you should add:

```sbt
githubTokenSource := TokenSource.GitConfig("github.token")
```

This assumes you have your token stored there like this:

```gitconfig
[github]
  token = TOKEN_DATA
```

The `||` combinator allows you to configure multiple token sources which will be tried in order on first-read of the setting.

Note that your CI server will need to set the `GITHUB_TOKEN` environment variable as well (if using the `Environment` token source), as well as any collaborators on your project. The environment-specific nature of these login credentials are a major part of why they are *not* just strings sitting in the `build.sbt` file. As an example, if you're using Travis, you can do something like the following:

```bash
# in your .profile file
$ export GITHUB_TOKEN="abcdef12345cafebabe"   # <-- your token here (or your build bot's)

# ...and when setting up your project
$ travis encrypt GITHUB_TOKEN=$GITHUB_TOKEN
```

If using GitHub Actions, the following is usually sufficient:

```yaml
env:
  GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
```

#### Token Permissions

GitHub Actions (and other instances of GitHub Apps) generate *different* tokens than those generated by the "Personal Access Tokens" section of your account settings. These tokens usually begin with `v1.`. They're weird because they are *not* associated with any particular user account. Thus, the value of `GITHUB_ACTOR` is irrelevant in such cases. It's entirely possible that `GITHUB_ACTOR` is irrelevant in *all* cases, but the API documentation claims otherwise. The API documentation claims many wrong things.

As an example, the documentation claims that, if you aren't publishing, your token only requires the `read: package` grant (and *not* `write: package`). Based on testing, as of right now, that appears to be false: `write: package` is required for *all* API calls against GitHub Packages, including resolution.

#### Manual Configuration

Once these values are set, the `credentials` key will be adjusted to reflect your GitHub authentication details. If you prefer, you are certainly free to set the credentials yourself, rather than trusting the plugin. They will need to take on the following form:

```sbt
credentials += 
  Credentials(
    "GitHub Package Registry",
    "maven.pkg.github.com",
    "USERNAME",
    "TOKEN")
```

Please, for the love of all that is holy, do not check this into your repository if you hard-code your credentials in this way. The token is a password. Treat it as such. A better approach would be to place the above into some global location, like `~/.sbt/1.0/github.sbt`.

### GitHub Actions

Okay, so GitHub Actions is pretty much undocumented with respect to its interactions with GitHub Packages. Through experimentation though, we've learned some important things.

The default token automagically-provided to all repositories works with GitHub Packages. So in other words, if you add `GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}` to your workflow's `env` section, things should work out just fine. The token in question is a JWT *bearer* token, not a conventional OAuth token.

Despite the fact that this token is documented as "scoped to the current repository", it will actually allow for *read* access to all public packages, not just in the current repository but in other repositories as well. 

It will NOT allow for read access to *private* packages within the same organization. You might see the following issue `[error]   not found: https://maven.pkg.github.com/...`. In order to pass, you have to create personal access token with [read:packages](https://developer.github.com/apps/building-oauth-apps/understanding-scopes-for-oauth-apps/#available-scopes) scope and use it `GITHUB_TOKEN: ${{ secrets.TOKEN_WITH_READ_PACKAGES_SCOPE }}`

### Keys

The following setting keys are defined:

- `githubOwner : String` Defines the organization or user name that owns the package registry to which this project will be published
- `githubRepository : String` The repository which hosts this project under the organization/user defined in the other setting
- `githubTokenSource : TokenSource` (*defaults to `Environment("GITHUB_TOKEN")`*) Where the plugin should go to read the GitHub API token to use in authentication. `TokenSource` has two possible values: `Environment(variable: String)` and `GitConfig(key: String)`. You can compose multiple sources together using `||`, which will result in each being attempted in order from left to right. This is mostly just a convenience. You're free to do whatever you want. Just don't, like, put it in your build. 
- `githubSuppressPublicationWarning : Boolean` (*defaults to `false`*) If you're just using this plugin as a means to *resolve* artifacts, not to publish them, the publication warning may serve as an annoyance more than anything else. Setting this to `true` will suppress the normal warning text when you fail to define `githubOwner` or `githubRepository`.
- `githubPublishTo : Option[Resolver]` The default `publishTo` target for GitHub Packages. This setting is useful for switching `publishTo` target to [sbt-sonatype](https://github.com/xerial/sbt-sonatype) or GitHub Packages: 

  ```scala
  // Switch publishTo target for using Sonatype if RELEASE_SONATYPE env is true, 
  // otherwise publish to GitHub Packages:
  val releaseToSonatype = sys.env.getOrElse("RELEASE_SONATYPE", "false").toBoolean 
  publishTo := {if (releaseToSonatype) sonatypePublishTo.value else githubPublishTo.value}

`homepage` and `scmInfo` will be automatically set for you if `githubOwner` and `githubRepository` are themselves set.
