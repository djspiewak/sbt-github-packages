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

### Resolvers

If you're consuming packages that were published in the GitHub Package Registry, this plugin defines some convenience syntax for adding resolvers:

```sbt
resolvers += Resolver.githubPackages("OWNER", "REPOSITORY")
```

This works for both public and private repositories, and you may add as many as desired, though if you use it with a private repository, you will also need to ensure that either `credentials` or `githubTokenSource` are appropriately configured.

You will need to ensure that both `githubActor` and `githubTokenSource` are set to *your* details (i.e. the authentication information for the individual who ran `sbt`). A relatively common setup for this is to add the following to your `~/.gitconfig` file (replacing `USERNAME` with whatever your username is):

```gitconfig
[github]
  actor = USERNAME
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

Environment variables are a fairly good default. For example, I have a GitHub token for my laptop stored in the `GITHUB_TOKEN` environment variable. If you mirror this setup, you should configure `githubTokenSource` in the following way:

```sbt
githubTokenSource := TokenSource.Environment("GITHUB_TOKEN")
```

The `||` combinator allows you to configure multiple token sources which will be tried in order on first-read of the setting.

Note that your CI server will need to set the `GITHUB_TOKEN` environment variable as well, as well as any collaborators on your project. The environment-specific nature of these login credentials are a major part of why they are *not* just strings sitting in the `build.sbt` file. As an example, if you're using Travis, you can do something like the following:

```bash
# in your .profile file
$ export GITHUB_TOKEN="abcdef12345cafebabe"   # <-- your token here (or your build bot's)

# ...and when setting up your project
$ travis encrypt GITHUB_TOKEN=$GITHUB_TOKEN
```

Then, in the `.travis.yml` file, you'll probably have the following (in addition to the `env.global` configuration that the `travis encrypt` command will prompt you to add):

```yaml
install:
  - git config --global git.user USERNAME
```

If you are solely *depending upon* packages and not publishing, the token only needs the `read:packages` grant. If you are *publishing*, the token will need the `write:packages` grant. You can provision a new token under your GitHub account under the [Personal access tokens](https://github.com/settings/tokens) section of **Settings**.

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

Okay, so GitHub Actions is pretty much undocumented with respect to its interactions with GitHub Packages. Lovely. Some things I've learned through experimentation:

- The automagical `${{ secrets.GITHUB_TOKEN }}`/`git config git.token` provided by GitHub Actions has access to packages in *other* public repositories, even when run from a pull request from a fork.
- The automagical token is not a standard access token, but rather a *bearer token* for the GitHub Actions, uh, App. Confused yet? Among other things, it means that the username is irrelevant. You can leave it blank, or rely on some other default.

The defaults for this plugin will work in GitHub Actions out of the box.

### Keys

The following setting keys are defined:

- `githubOwner : String` Defines the organization or user name that owns the package registry to which this project will be published
- `githubRepository : String` The repository which hosts this project under the organization/user defined in the other setting
- `githubActor : String` (*defaults to `git config github.actor`*) *Your* GitHub username. This should almost never be specified in the build itself, but rather read from some external source. By default, it will read from the `git config` (by shelling out to the `git` command), but it's easy to override this to use an environment variable (e.g. `githubActor := sys.env("GITHUB_ACTOR")`). To be extremely clear, this is the user who ran the `sbt` command, it is not *necessarily* the repository owner!
- `githubTokenSource : TokenSource` (*defaults to `GitConfig("github.token")`*) Where the plugin should go to read the GitHub API token to use in authentication. `TokenSource` has two possible values: `Environment(variable: String)` and `GitConfig(key: String)`. This is mostly just a convenience. You're free to do whatever you want. Just don't, like, put it in your build. 

`homepage` and `scmInfo` will be automatically set for you if `githubOwner` and `githubRepository` are themselves set.
