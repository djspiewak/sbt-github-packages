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

### Resolvers

If you're consuming packages that were published in the GitHub Package Registry, this plugin defines some convenience syntax for adding resolvers:

```sbt
resolvers += Resolver.githubPackages("OWNER")
```

You may also *optionally* specify a repository as the second argument. **This is not required!** By default, sbt-github-packages will attempt to resolve from a repository named "_", which does not need to exist. If that repository *does* exist, and it is private, then the token used in authentication must have access to private repositories in that organization. In most cases, just the owner parameter will be sufficient.

This resolver will give you access to packages published on *any* repository within the organization. If the token provided in the authentication information only has access to public repositories, then packages published on private repositories will report "not found". If the token has access to private repositories as well as public, then all packages will be visible.

You will need to ensure that both `githubActor` and `githubTokenSource` are set to *your* details (i.e. the authentication information for the individual who ran `sbt`). A relatively common setup for this is to use environment variables. For example, my `~/.profile` contains the following lines:

```bash
export GITHUB_ACTOR=djspiewak
export GITHUB_TOKEN='<redacted>'
```

Once this is configured, sbt-github-packages will automatically set your `githubActor` key to its value. That just leaves the `githubTokenSource`. The `TokenSource` ADT has the following possibilities:

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

As mentioned earlier, environment variables are a good default. For example, I have a GitHub token for my laptop stored in the `GITHUB_TOKEN` environment variable. If you mirror this setup, you should configure `githubTokenSource` in the following way:

```sbt
githubTokenSource := TokenSource.Environment("GITHUB_TOKEN")
```

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

Note that your CI server will need to set the `GITHUB_TOKEN` environment variable as well, as well as any collaborators on your project. The environment-specific nature of these login credentials are a major part of why they are *not* just strings sitting in the `build.sbt` file. As an example, if you're using Travis, you can do something like the following:

```bash
# in your .profile file
$ export GITHUB_TOKEN="abcdef12345cafebabe"   # <-- your token here (or your build bot's)

# ...and when setting up your project
$ travis encrypt GITHUB_TOKEN=$GITHUB_TOKEN
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

The default token automagically-provided to all repositories works with GitHub Packages. So in other words, if you add `GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}` to your workflow's `env` section, things should work out just fine. The token in question is a JWT *bearer* token, not a conventional OAuth token, and so the `GITHUB_ACTOR` value is irrelevant *but must still be set*. Set `GITHUB_ACTOR` to any valid username.

### Keys

The following setting keys are defined:

- `githubOwner : String` Defines the organization or user name that owns the package registry to which this project will be published
- `githubRepository : String` The repository which hosts this project under the organization/user defined in the other setting
- `githubActor : String` (*defaults to `GITHUB_ACTOR`*) *Your* GitHub username. This should almost never be specified in the build itself, but rather read from some external source. By default, it will read the `GITHUB_ACTOR` environment variable. To be extremely clear, this is the user who ran the `sbt` command, it is not *necessarily* the repository owner!
- `githubTokenSource : TokenSource` (*defaults to `Environment("GITHUB_TOKEN")`*) Where the plugin should go to read the GitHub API token to use in authentication. `TokenSource` has two possible values: `Environment(variable: String)` and `GitConfig(key: String)`. You can compose multiple sources together using `||`, which will result in each being attempted in order from left to right. This is mostly just a convenience. You're free to do whatever you want. Just don't, like, put it in your build. 

`homepage` and `scmInfo` will be automatically set for you if `githubOwner` and `githubRepository` are themselves set.
